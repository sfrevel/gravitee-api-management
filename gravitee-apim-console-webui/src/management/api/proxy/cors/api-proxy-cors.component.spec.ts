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
import { MatSlideToggleHarness } from '@angular/material/slide-toggle/testing';
import { GioFormTagsInputHarness, GioSaveBarHarness } from '@gravitee/ui-particles-angular';
import { MatSelectHarness } from '@angular/material/select/testing';
import { MatInputHarness } from '@angular/material/input/testing';
import { MatDialogHarness } from '@angular/material/dialog/testing';
import { MatButtonHarness } from '@angular/material/button/testing';
import { InteractivityChecker } from '@angular/cdk/a11y';

import { ApiProxyCorsComponent } from './api-proxy-cors.component';
import { ApiProxyCorsModule } from './api-proxy-cors.module';

import { UIRouterStateParams, CurrentUserService, AjsRootScope } from '../../../../ajs-upgraded-providers';
import { User } from '../../../../entities/user';
import { Api } from '../../../../entities/api';
import { fakeApi } from '../../../../entities/api/Api.fixture';
import { CONSTANTS_TESTING, GioHttpTestingModule } from '../../../../shared/testing';

describe('ApiProxyEntrypointsComponent', () => {
  const API_ID = 'apiId';

  const currentUser = new User();
  currentUser.userPermissions = ['api-definition-u'];

  let fixture: ComponentFixture<ApiProxyCorsComponent>;
  let loader: HarnessLoader;
  let rootLoader: HarnessLoader;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [NoopAnimationsModule, GioHttpTestingModule, ApiProxyCorsModule],
      providers: [
        { provide: UIRouterStateParams, useValue: { apiId: API_ID } },
        { provide: CurrentUserService, useValue: { currentUser } },
        { provide: AjsRootScope, useValue: null },
      ],
    }).overrideProvider(InteractivityChecker, {
      useValue: {
        isFocusable: () => true, // This checks focus trap, set it to true to  avoid the warning
      },
    });
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ApiProxyCorsComponent);
    loader = TestbedHarnessEnvironment.loader(fixture);
    rootLoader = TestbedHarnessEnvironment.documentRootLoader(fixture);

    httpTestingController = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should enable and set CORS config', async () => {
    const api = fakeApi({
      id: API_ID,
      proxy: {
        cors: {
          enabled: false,
        },
      },
    });
    expectApiGetRequest(api);
    const saveBar = await loader.getHarness(GioSaveBarHarness);
    expect(await saveBar.isVisible()).toBe(false);

    const enabledSlideToggle = await loader.getHarness(MatSlideToggleHarness.with({ selector: '[formControlName="enabled"]' }));
    expect(await enabledSlideToggle.isChecked()).toEqual(false);

    // Check each field is disabled
    const allowOriginInput = await loader.getHarness(GioFormTagsInputHarness.with({ selector: '[formControlName="allowOrigin"]' }));
    expect(await allowOriginInput.isDisabled()).toEqual(true);

    const allowMethodsInput = await loader.getHarness(MatSelectHarness.with({ selector: '[formControlName="allowMethods"]' }));
    expect(await allowMethodsInput.isDisabled()).toEqual(true);

    // Enable Cors & set some values
    await enabledSlideToggle.toggle();

    await allowOriginInput.addTag('toto');
    await allowMethodsInput.clickOptions({ text: 'GET' });

    expect(await saveBar.isSubmitButtonInvalid()).toEqual(false);
    await saveBar.clickSubmit();

    // Expect fetch api and update
    expectApiGetRequest(api);
    const req = httpTestingController.expectOne({ method: 'PUT', url: `${CONSTANTS_TESTING.env.baseURL}/apis/${API_ID}` });
    expect(req.request.body.proxy.cors).toStrictEqual({
      enabled: true,
      allowMethods: ['GET'],
      allowOrigin: ['toto'],
      allowHeaders: [],
      allowCredentials: false,
      exposeHeaders: [],
      maxAge: -1,
      runPolicies: false,
    });
  });

  it('should update CORS config', async () => {
    const api = fakeApi({
      id: API_ID,
      proxy: {
        cors: {
          enabled: true,
          allowOrigin: ['allowOrigin'],
          allowMethods: ['GET'],
          allowHeaders: ['allowHeaders'],
          allowCredentials: true,
          maxAge: 10,
          exposeHeaders: ['exposeHeaders'],
          runPolicies: true,
        },
      },
    });
    expectApiGetRequest(api);
    const saveBar = await loader.getHarness(GioSaveBarHarness);

    const enabledSlideToggle = await loader.getHarness(MatSlideToggleHarness.with({ selector: '[formControlName="enabled"]' }));
    expect(await enabledSlideToggle.isChecked()).toEqual(true);

    const allowOriginInput = await loader.getHarness(GioFormTagsInputHarness.with({ selector: '[formControlName="allowOrigin"]' }));
    expect(await allowOriginInput.getTags()).toEqual(['allowOrigin']);
    await allowOriginInput.removeTag('allowOrigin');

    const allowMethodsInput = await loader.getHarness(MatSelectHarness.with({ selector: '[formControlName="allowMethods"]' }));
    await allowMethodsInput.clickOptions({ text: 'GET' });

    const allowHeadersInput = await loader.getHarness(GioFormTagsInputHarness.with({ selector: '[formControlName="allowHeaders"]' }));
    expect(await allowHeadersInput.getTags()).toEqual(['allowHeaders']);
    await allowHeadersInput.addTag('allowHeaders2');

    const allowCredentialsInput = await loader.getHarness(MatSlideToggleHarness.with({ selector: '[formControlName="allowCredentials"]' }));
    expect(await allowCredentialsInput.isChecked()).toEqual(true);
    await allowCredentialsInput.toggle();

    const maxAgeInput = await loader.getHarness(MatInputHarness.with({ selector: '[formControlName="maxAge"]' }));
    expect(await maxAgeInput.getValue()).toEqual('10');
    await maxAgeInput.setValue('20');

    const exposeHeadersInput = await loader.getHarness(GioFormTagsInputHarness.with({ selector: '[formControlName="exposeHeaders"]' }));
    expect(await exposeHeadersInput.getTags()).toEqual(['exposeHeaders']);
    await exposeHeadersInput.addTag('exposeHeaders2');

    const runPoliciesInput = await loader.getHarness(MatSlideToggleHarness.with({ selector: '[formControlName="runPolicies"]' }));
    expect(await runPoliciesInput.isChecked()).toEqual(true);
    await runPoliciesInput.toggle();

    expect(await saveBar.isSubmitButtonInvalid()).toEqual(false);
    await saveBar.clickSubmit();

    // Expect fetch api and update
    expectApiGetRequest(api);
    const req = httpTestingController.expectOne({ method: 'PUT', url: `${CONSTANTS_TESTING.env.baseURL}/apis/${API_ID}` });
    expect(req.request.body.proxy.cors).toStrictEqual({
      enabled: true,
      allowOrigin: [],
      allowMethods: [],
      allowHeaders: ['allowHeaders', 'allowHeaders2'],
      allowCredentials: false,
      maxAge: 20,
      exposeHeaders: ['exposeHeaders', 'exposeHeaders2'],
      runPolicies: false,
    });
  });

  it('should open confirm dialog for the addition of a Allow-Origin with `*`', async () => {
    const api = fakeApi({
      id: API_ID,
      proxy: {
        cors: {
          enabled: true,
        },
      },
    });
    expectApiGetRequest(api);

    const allowOriginInput = await loader.getHarness(GioFormTagsInputHarness.with({ selector: '[formControlName="allowOrigin"]' }));
    expect(await allowOriginInput.getTags()).toEqual([]);
    // Add `*` and confirm dialog
    await allowOriginInput.addTag('*');
    const dialogOne = await rootLoader.getHarness(MatDialogHarness);
    expect(await dialogOne.getId()).toEqual('allowAllOriginsConfirmDialog');
    await dialogOne.close();

    await allowOriginInput.addTag('*');
    const dialogTwo = await rootLoader.getHarness(MatDialogHarness);
    await (await dialogTwo.getHarness(MatButtonHarness.with({ text: /^Yes,/ }))).click();

    const saveButton = await loader.getHarness(GioSaveBarHarness);
    await saveButton.clickSubmit();

    // Expect fetch api and update
    expectApiGetRequest(api);
    const req = httpTestingController.expectOne({ method: 'PUT', url: `${CONSTANTS_TESTING.env.baseURL}/apis/${API_ID}` });
    expect(req.request.body.proxy.cors).toStrictEqual({
      enabled: true,
      allowMethods: [],
      allowOrigin: ['*'],
      allowHeaders: [],
      allowCredentials: false,
      exposeHeaders: [],
      maxAge: -1,
      runPolicies: false,
    });
  });

  it('should disable field when origin is kubernetes', async () => {
    const api = fakeApi({
      id: API_ID,
      proxy: {
        cors: {
          enabled: true,
        },
      },
      definition_context: { origin: 'kubernetes' },
    });
    expectApiGetRequest(api);

    const saveBar = await loader.getHarness(GioSaveBarHarness);
    expect(await saveBar.isVisible()).toBe(false);

    const allowMethodsInput = await loader.getHarness(MatSelectHarness.with({ selector: '[formControlName="allowMethods"]' }));
    expect(await allowMethodsInput.isDisabled()).toEqual(true);

    const enabledSlideToggle = await loader.getHarness(MatSlideToggleHarness.with({ selector: '[formControlName="enabled"]' }));
    expect(await enabledSlideToggle.isDisabled()).toEqual(true);
  });

  function expectApiGetRequest(api: Api) {
    httpTestingController.expectOne({ url: `${CONSTANTS_TESTING.env.baseURL}/apis/${api.id}`, method: 'GET' }).flush(api);
    fixture.detectChanges();
  }
});
