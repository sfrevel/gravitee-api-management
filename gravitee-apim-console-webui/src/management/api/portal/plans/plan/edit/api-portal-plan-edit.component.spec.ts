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
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { HttpTestingController } from '@angular/common/http/testing';
import { HarnessLoader } from '@angular/cdk/testing';
import { TestbedHarnessEnvironment } from '@angular/cdk/testing/testbed';
import { GioFormTagsInputHarness, GioSaveBarHarness } from '@gravitee/ui-particles-angular';
import { MatInputHarness } from '@angular/material/input/testing';
import { MatSelectHarness } from '@angular/material/select/testing';
import { MatSlideToggleHarness } from '@angular/material/slide-toggle/testing';
import { MatIconTestingModule } from '@angular/material/icon/testing';
import { set } from 'lodash';

import { ApiPortalPlanEditComponent } from './api-portal-plan-edit.component';
import { ApiPortalPlanEditModule } from './api-portal-plan-edit.module';

import { CurrentUserService, UIRouterStateParams } from '../../../../../../ajs-upgraded-providers';
import { User } from '../../../../../../entities/user';
import { CONSTANTS_TESTING, GioHttpTestingModule } from '../../../../../../shared/testing';
import { Tag } from '../../../../../../entities/tag/tag';
import { Group } from '../../../../../../entities/group/group';
import { fakeGroup } from '../../../../../../entities/group/group.fixture';
import { fakeTag } from '../../../../../../entities/tag/tag.fixture';
import { Page } from '../../../../../../entities/page';
import { fakeApi } from '../../../../../../entities/api/Api.fixture';
import { Api } from '../../../../../../entities/api';

describe('ApiPortalPlanEditComponent', () => {
  const API_ID = 'my-api';
  const currentUser = new User();
  currentUser.userPermissions = ['api-plan-u'];

  let fixture: ComponentFixture<ApiPortalPlanEditComponent>;
  let loader: HarnessLoader;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [NoopAnimationsModule, GioHttpTestingModule, ApiPortalPlanEditModule, MatIconTestingModule],
      providers: [
        { provide: CurrentUserService, useValue: { currentUser } },
        { provide: UIRouterStateParams, useValue: { apiId: API_ID } },
        {
          provide: 'Constants',
          useFactory: () => {
            const constants = CONSTANTS_TESTING;
            set(constants, 'env.settings.plan.security', {
              oauth2: { enabled: false },
              jwt: { enabled: true },
            });
            return constants;
          },
        },
      ],
    });
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ApiPortalPlanEditComponent);
    loader = TestbedHarnessEnvironment.loader(fixture);
    TestbedHarnessEnvironment.documentRootLoader(fixture);

    httpTestingController = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should add new plan', async () => {
    const TAG_1_ID = 'tag-1';

    expectApiGetRequest(fakeApi({ id: API_ID, tags: [TAG_1_ID] }));
    expectTagsListRequest([fakeTag({ id: TAG_1_ID, name: 'Tag 1' }), fakeTag({ id: 'tag-2', name: 'Tag 2' })]);
    expectGroupLisRequest([fakeGroup({ id: 'group-a', name: 'Group A' })]);
    expectDocumentationSearchRequest(API_ID, [{ id: 'doc-1', name: 'Doc 1' }]);
    expectCurrentUserTagsRequest([TAG_1_ID]);
    expectResourceGetRequest();
    fixture.detectChanges();

    const saveBar = await loader.getHarness(GioSaveBarHarness);
    expect(await saveBar.isVisible()).toBe(true);

    // 1- General Step
    const nameInput = await loader.getHarness(MatInputHarness.with({ selector: '[formControlName="name"]' }));
    await nameInput.setValue('🗺');

    const descriptionInput = await loader.getHarness(MatInputHarness.with({ selector: '[formControlName="description"]' }));
    await descriptionInput.setValue('Description');

    const characteristicsInput = await loader.getHarness(GioFormTagsInputHarness.with({ selector: '[formControlName="characteristics"]' }));
    await characteristicsInput.addTag('C1');

    const generalConditionsInput = await loader.getHarness(MatSelectHarness.with({ selector: '[formControlName="generalConditions"]' }));
    await generalConditionsInput.clickOptions({ text: 'Doc 1' });

    const validationToggle = await loader.getHarness(MatSlideToggleHarness.with({ selector: '[formControlName="validation"]' }));
    await validationToggle.toggle();

    const commentRequired = await loader.getHarness(MatSlideToggleHarness.with({ selector: '[formControlName="commentRequired"]' }));
    await commentRequired.toggle();

    const commentMessageInput = await loader.getHarness(MatInputHarness.with({ selector: '[formControlName="commentMessage"]' }));
    await commentMessageInput.setValue('Comment message');

    const shardingTagsInput = await loader.getHarness(MatSelectHarness.with({ selector: '[formControlName="shardingTags"]' }));
    await shardingTagsInput.clickOptions({ text: /Tag 1/ });
    await shardingTagsInput.getOptions({ text: /Tag 2/ }).then((options) => options[0].isDisabled());

    const excludedGroupsInput = await loader.getHarness(MatSelectHarness.with({ selector: '[formControlName="excludedGroups"]' }));
    await excludedGroupsInput.clickOptions({ text: 'Group A' });

    // 2- Secure Step
    const securityTypesInput = await loader.getHarness(MatSelectHarness.with({ selector: '[formControlName="securityTypes"]' }));
    await securityTypesInput.clickOptions({ text: /JWT/ });
    await securityTypesInput.getOptions({ text: /OAuth2/ }).then((options) => expect(options.length).toBe(0));
    expectPolicySchemaGetRequest('jwt', {});

    const selectionRuleInput = await loader.getHarness(MatInputHarness.with({ selector: '[formControlName="selectionRule"]' }));
    await selectionRuleInput.setValue('{ #el ...}');

    expect(fixture.componentInstance.planForm.getRawValue()).toEqual({
      general: {
        name: '🗺',
        description: 'Description',
        characteristics: ['C1'],
        generalConditions: 'doc-1',
        shardingTags: [TAG_1_ID],
        commentRequired: true,
        commentMessage: 'Comment message',
        validation: true,
        excludedGroups: ['group-a'],
      },
      secure: {
        securityConfig: {},
        securityTypes: 'JWT',
        selectionRule: '{ #el ...}',
      },
    });
  });

  function expectTagsListRequest(tags: Tag[] = []) {
    httpTestingController
      .expectOne({
        method: 'GET',
        url: `${CONSTANTS_TESTING.org.baseURL}/configuration/tags`,
      })
      .flush(tags);
  }

  function expectGroupLisRequest(groups: Group[] = []) {
    httpTestingController
      .expectOne({
        method: 'GET',
        url: `${CONSTANTS_TESTING.env.baseURL}/configuration/groups`,
      })
      .flush(groups);
  }

  function expectDocumentationSearchRequest(apiId: string, groups: Page[] = []) {
    httpTestingController
      .expectOne({
        method: 'GET',
        url: `${CONSTANTS_TESTING.env.baseURL}/apis/${apiId}/pages?type=MARKDOWN&api=${apiId}`,
      })
      .flush(groups);
  }

  function expectApiGetRequest(api: Api) {
    httpTestingController.expectOne({ url: `${CONSTANTS_TESTING.env.baseURL}/apis/${api.id}`, method: 'GET' }).flush(api);
  }

  function expectCurrentUserTagsRequest(tags: string[]) {
    httpTestingController
      .expectOne({
        method: 'GET',
        url: `${CONSTANTS_TESTING.org.baseURL}/user/tags`,
      })
      .flush(tags);
  }

  function expectResourceGetRequest() {
    httpTestingController.expectOne({ url: `${CONSTANTS_TESTING.env.baseURL}/resources?expand=icon`, method: 'GET' }).flush([]);
  }

  function expectPolicySchemaGetRequest(type: string, schema: unknown) {
    httpTestingController.expectOne({ url: `${CONSTANTS_TESTING.env.baseURL}/policies/${type}/schema`, method: 'GET' }).flush(schema);
  }
});
