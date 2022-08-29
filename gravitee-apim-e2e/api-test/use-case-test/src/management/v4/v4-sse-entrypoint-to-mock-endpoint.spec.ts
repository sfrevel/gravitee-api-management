/*
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import { afterAll, beforeAll, describe, expect } from '@jest/globals';
import { forManagementAsApiUser } from '@gravitee/utils/configuration';
import { APIPlansV4Api } from '@gravitee/management-webclient-sdk/src/lib/apis/APIPlansV4Api';
import { APIsV4Api } from '@gravitee/management-webclient-sdk/src/lib/apis/APIsV4Api';
import { ApiEntityV4, LifecycleAction, PlanEntityV4 } from '@gravitee/management-webclient-sdk/src/lib/models';
import { fetchEventSourceGateway } from '@gravitee/utils/gateway';
import { PlansV4Faker } from '@gravitee/fixtures/management/PlansV4Faker';
import { ApisV4Faker } from '@gravitee/fixtures/management/ApisV4Faker';
import faker from '@faker-js/faker';
import { describeIfJupiter } from '@lib/jest-utils';

const orgId = 'DEFAULT';
const envId = 'DEFAULT';

const apisResource = new APIsV4Api(forManagementAsApiUser());
const apiPlansResource = new APIPlansV4Api(forManagementAsApiUser());

describeIfJupiter('Gateway V4 - SSE entrypoint to mock endpoint', () => {
  const contextPath = `${faker.random.word()}-${faker.datatype.uuid()}-${Math.floor(Date.now() / 1000)}`;
  let createdApi: ApiEntityV4;
  let createdKeylessPlan: PlanEntityV4;

  beforeAll(async () => {
    // create a V4 API with an SSE entrypoint and a mock endpoint
    createdApi = await apisResource.createApi1({
      orgId,
      envId,
      newApiEntityV4: ApisV4Faker.newApi({
        listeners: [
          ApisV4Faker.newHttpListener({
            paths: [
              {
                path: '/' + contextPath + '/',
              },
            ],
            entrypoints: [
              {
                type: 'sse',
              },
            ],
          }),
        ],
        endpointGroups: [
          {
            name: 'default',
            type: 'mock',
            endpoints: [
              {
                name: 'default',
                type: 'mock',
                weight: 1,
                configuration: {
                  messageInterval: 100,
                  messageContent: 'e2e test message',
                  messageCount: 3,
                },
              },
            ],
          },
        ],
      }),
    });

    // create a keyless plan
    createdKeylessPlan = await apisResource.createApiPlan1({
      envId,
      orgId,
      api: createdApi.id,
      newPlanEntityV4: PlansV4Faker.newPlan(),
    });

    // publish the plan
    await apiPlansResource.publishApiPlan1({
      envId,
      orgId,
      api: createdApi.id,
      plan: createdKeylessPlan.id,
    });

    // start the API
    await apisResource.doApiLifecycleAction1({
      envId,
      orgId,
      api: createdApi.id,
      action: LifecycleAction.START,
    });
  });

  describe('Gateway call', () => {
    test('Gateway call should return backend response', async () => {
      const messages = [];
      await fetchEventSourceGateway({ contextPath: `/${contextPath}` }, (ev) => {
        messages.push(ev.data);
      });
      expect(messages).toHaveLength(3);
      expect(messages[0]).toBe('e2e test message 0');
      expect(messages[1]).toBe('e2e test message 1');
      expect(messages[2]).toBe('e2e test message 2');
    });
  });

  afterAll(async () => {
    if (createdApi) {
      // stop API
      await apisResource.doApiLifecycleAction1({
        envId,
        orgId,
        api: createdApi.id,
        action: LifecycleAction.STOP,
      });

      // close plan
      await apiPlansResource.closeApiPlan1({
        envId,
        orgId,
        plan: createdKeylessPlan.id,
        api: createdApi.id,
      });

      // delete API
      await apisResource.deleteApi1({
        envId,
        orgId,
        api: createdApi.id,
      });
    }
  });
});