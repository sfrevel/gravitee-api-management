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
import { MatInputHarness } from '@angular/material/input/testing';
import { GioFormHeadersHarness, GioSaveBarHarness } from '@gravitee/ui-particles-angular';
import { cloneDeep } from 'lodash';
import { MatIconTestingModule } from '@angular/material/icon/testing';

import { ApiProxyResponseTemplatesEditComponent } from './api-proxy-response-templates-edit.component';

import { CONSTANTS_TESTING, GioHttpTestingModule } from '../../../../../shared/testing';
import { ApiProxyResponseTemplatesModule } from '../api-proxy-response-templates.module';
import { UIRouterStateParams, UIRouterState, CurrentUserService, AjsRootScope } from '../../../../../ajs-upgraded-providers';
import { User } from '../../../../../entities/user';
import { fakeApi } from '../../../../../entities/api/Api.fixture';
import { Api } from '../../../../../entities/api';

describe('ApiProxyResponseTemplatesComponent', () => {
  const API_ID = 'apiId';
  const fakeUiRouter = { go: jest.fn() };

  let fixture: ComponentFixture<ApiProxyResponseTemplatesEditComponent>;
  let loader: HarnessLoader;
  let httpTestingController: HttpTestingController;

  const currentUser = new User();
  currentUser.userPermissions = ['api-response_templates-c', 'api-response_templates-u', 'api-response_templates-d'];

  const initTestingComponent = (responseTemplateId?: string) => {
    TestBed.configureTestingModule({
      imports: [NoopAnimationsModule, GioHttpTestingModule, ApiProxyResponseTemplatesModule, MatIconTestingModule],
      providers: [
        { provide: UIRouterStateParams, useValue: { apiId: API_ID, responseTemplateId } },
        { provide: UIRouterState, useValue: fakeUiRouter },
        { provide: CurrentUserService, useValue: { currentUser } },
        { provide: AjsRootScope, useValue: null },
      ],
    });
    fixture = TestBed.createComponent(ApiProxyResponseTemplatesEditComponent);
    loader = TestbedHarnessEnvironment.loader(fixture);

    httpTestingController = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  };

  afterEach(() => {
    httpTestingController.verify();
  });

  describe('creation mode', () => {
    beforeEach(() => {
      initTestingComponent();
    });

    it('should add new response template', async () => {
      const api = fakeApi({
        id: API_ID,
      });
      expectApiGetRequest(api);

      const saveBar = await loader.getHarness(GioSaveBarHarness);
      expect(await saveBar.isVisible()).toBe(true);

      const keyInput = await loader.getHarness(MatInputHarness.with({ selector: '[formControlName="key"]' }));
      expect(await keyInput.isDisabled()).toEqual(false);
      await keyInput.setValue('newTemplateKey');

      const acceptHeaderInput = await loader.getHarness(MatInputHarness.with({ selector: '[formControlName="acceptHeader"]' }));
      await acceptHeaderInput.setValue('application/json');

      const statusCodeInput = await loader.getHarness(MatInputHarness.with({ selector: '[formControlName="statusCode"]' }));
      await statusCodeInput.setValue('200');

      const headersInput = await loader.getHarness(GioFormHeadersHarness.with({ selector: '[formControlName="headers"]' }));
      headersInput.addHeader({ key: 'header1', value: 'value1' });

      const bodyInput = await loader.getHarness(MatInputHarness.with({ selector: '[formControlName="body"]' }));
      await bodyInput.setValue('newTemplateBody');

      await saveBar.clickSubmit();

      // Expect fetch api and update
      expectApiGetRequest(api);
      const req = httpTestingController.expectOne({ method: 'PUT', url: `${CONSTANTS_TESTING.env.baseURL}/apis/${API_ID}` });
      expect(req.request.body.response_templates).toEqual({
        ...api.response_templates,
        newTemplateKey: { 'application/json': { status: 200, headers: undefined, body: 'newTemplateBody' } },
      });
    });

    it('should add new response template to existing template key', async () => {
      const api = fakeApi({
        id: API_ID,
      });
      expectApiGetRequest(api);

      const saveBar = await loader.getHarness(GioSaveBarHarness);
      expect(await saveBar.isVisible()).toBe(true);

      const keyInput = await loader.getHarness(MatInputHarness.with({ selector: '[formControlName="key"]' }));
      expect(await keyInput.isDisabled()).toEqual(false);
      await keyInput.setValue('DEFAULT');

      const acceptHeaderInput = await loader.getHarness(MatInputHarness.with({ selector: '[formControlName="acceptHeader"]' }));
      await acceptHeaderInput.setValue('application/json');

      await saveBar.clickSubmit();

      // Expect fetch api and update
      expectApiGetRequest(api);
      const req = httpTestingController.expectOne({ method: 'PUT', url: `${CONSTANTS_TESTING.env.baseURL}/apis/${API_ID}` });
      expect(req.request.body.response_templates).toEqual({
        ...api.response_templates,
        ['DEFAULT']: {
          ...api.response_templates['DEFAULT'],
          'application/json': { status: 400, body: '' },
        },
      });
    });
  });

  describe('edition mode', () => {
    beforeEach(() => {
      initTestingComponent('DEFAULT-*/*');
    });

    it('should edit response template', async () => {
      const api = fakeApi({
        id: API_ID,
      });
      expectApiGetRequest(api);

      const saveBar = await loader.getHarness(GioSaveBarHarness);
      expect(await saveBar.isVisible()).toBe(false);

      const keyInput = await loader.getHarness(MatInputHarness.with({ selector: '[formControlName="key"]' }));
      expect(await keyInput.isDisabled()).toEqual(false);
      expect(await keyInput.getValue()).toEqual('DEFAULT');

      const acceptHeaderInput = await loader.getHarness(MatInputHarness.with({ selector: '[formControlName="acceptHeader"]' }));
      expect(await acceptHeaderInput.getValue()).toEqual('*/*');

      const statusCodeInput = await loader.getHarness(MatInputHarness.with({ selector: '[formControlName="statusCode"]' }));
      expect(await statusCodeInput.getValue()).toEqual('400');

      const headersInput = await loader.getHarness(GioFormHeadersHarness.with({ selector: '[formControlName="headers"]' }));
      await headersInput.addHeader({ key: 'header1', value: 'value1' });

      const bodyInput = await loader.getHarness(MatInputHarness.with({ selector: '[formControlName="body"]' }));
      await bodyInput.setValue('newTemplateBody');

      await saveBar.clickSubmit();

      // Expect fetch api and update
      expectApiGetRequest(api);
      const req = httpTestingController.expectOne({ method: 'PUT', url: `${CONSTANTS_TESTING.env.baseURL}/apis/${API_ID}` });
      expect(req.request.body.response_templates).toEqual({
        ...api.response_templates,
        ['DEFAULT']: {
          ...api.response_templates['DEFAULT'],
          '*/*': {
            status: 400,
            headers: {
              header1: 'value1',
            },
            body: 'newTemplateBody',
          },
        },
      });
    });

    it('should edit response template key and accept header', async () => {
      const api = fakeApi({
        id: API_ID,
      });
      expectApiGetRequest(api);

      const saveBar = await loader.getHarness(GioSaveBarHarness);
      expect(await saveBar.isVisible()).toBe(false);

      const keyInput = await loader.getHarness(MatInputHarness.with({ selector: '[formControlName="key"]' }));
      expect(await keyInput.isDisabled()).toEqual(false);
      expect(await keyInput.getValue()).toEqual('DEFAULT');
      await keyInput.setValue('NewKey');

      const acceptHeaderInput = await loader.getHarness(MatInputHarness.with({ selector: '[formControlName="acceptHeader"]' }));
      await acceptHeaderInput.setValue('new/accept');

      await saveBar.clickSubmit();

      // Expect fetch api and update
      expectApiGetRequest(api);

      const expectedResponseTemplates = cloneDeep(api.response_templates);
      delete expectedResponseTemplates['DEFAULT']['*/*'];
      expectedResponseTemplates.NewKey = {
        'new/accept': { status: 400, body: '' },
      };

      const req = httpTestingController.expectOne({ method: 'PUT', url: `${CONSTANTS_TESTING.env.baseURL}/apis/${API_ID}` });
      expect(req.request.body.response_templates).toEqual(expectedResponseTemplates);
    });

    it('should throw if new template key and accept header already exist', async () => {
      const api = fakeApi({
        id: API_ID,
      });
      expectApiGetRequest(api);

      const saveBar = await loader.getHarness(GioSaveBarHarness);
      expect(await saveBar.isVisible()).toBe(false);

      const keyInput = await loader.getHarness(MatInputHarness.with({ selector: '[formControlName="key"]' }));
      const acceptHeaderInput = await loader.getHarness(MatInputHarness.with({ selector: '[formControlName="acceptHeader"]' }));

      // Fake api already have a template with key `DEFAULT` and accept header `test`
      await acceptHeaderInput.setValue('test');
      expect(await saveBar.isSubmitButtonInvalid()).toBe(true);

      // Ok no template with key `customKey` and accept header `test`
      await keyInput.setValue('customKey');
      expect(await saveBar.isSubmitButtonInvalid()).toBe(false);

      // Fake api already have a template with key `customKey` and accept header `*/*`
      await acceptHeaderInput.setValue('*/*');
      expect(await saveBar.isSubmitButtonInvalid()).toBe(true);
    });

    it('should disable field when origin is kubernetes', async () => {
      const api = fakeApi({
        id: API_ID,
        definition_context: {
          origin: 'kubernetes',
        },
        response_templates: {
          DEFAULT: {
            '*/*': {
              body: 'json',
              status: 400,
            },
          },
        },
      });
      expectApiGetRequest(api);

      const keyInput = await loader.getHarness(MatInputHarness.with({ selector: '[formControlName="key"]' }));
      expect(await keyInput.isDisabled()).toEqual(true);
      expect(await keyInput.getValue()).toEqual('DEFAULT');

      const acceptHeaderInput = await loader.getHarness(MatInputHarness.with({ selector: '[formControlName="acceptHeader"]' }));
      expect(await acceptHeaderInput.isDisabled()).toEqual(true);
      expect(await acceptHeaderInput.getValue()).toEqual('*/*');

      const statusCodeInput = await loader.getHarness(MatInputHarness.with({ selector: '[formControlName="statusCode"]' }));
      expect(await statusCodeInput.isDisabled()).toEqual(true);
      expect(await statusCodeInput.getValue()).toEqual('400');

      const headersInput = await loader.getHarness(GioFormHeadersHarness.with({ selector: '[formControlName="headers"]' }));
      expect(await headersInput.isDisabled()).toEqual(true);

      const bodyInput = await loader.getHarness(MatInputHarness.with({ selector: '[formControlName="body"]' }));
      expect(await bodyInput.isDisabled()).toEqual(true);
      expect(await bodyInput.getValue()).toEqual('json');
    });
  });

  function expectApiGetRequest(api: Api) {
    httpTestingController.expectOne({ url: `${CONSTANTS_TESTING.env.baseURL}/apis/${api.id}`, method: 'GET' }).flush(api);
    fixture.detectChanges();
  }
});
