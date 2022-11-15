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
import { GioSaveBarHarness } from '@gravitee/ui-particles-angular';
import { MatInputHarness } from '@angular/material/input/testing';
import { InteractivityChecker } from '@angular/cdk/a11y';

import { ApiProxyFailoverComponent } from './api-proxy-failover.component';
import { ApiProxyFailoverModule } from './api-proxy-failover.module';

import { UIRouterStateParams, CurrentUserService, AjsRootScope } from '../../../../ajs-upgraded-providers';
import { User } from '../../../../entities/user';
import { Api } from '../../../../entities/api';
import { fakeApi } from '../../../../entities/api/Api.fixture';
import { CONSTANTS_TESTING, GioHttpTestingModule } from '../../../../shared/testing';

describe('ApiProxyFailoverComponent', () => {
  const API_ID = 'apiId';

  const currentUser = new User();
  currentUser.userPermissions = ['api-definition-u'];

  let fixture: ComponentFixture<ApiProxyFailoverComponent>;
  let loader: HarnessLoader;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [NoopAnimationsModule, GioHttpTestingModule, ApiProxyFailoverModule],
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
    fixture = TestBed.createComponent(ApiProxyFailoverComponent);
    loader = TestbedHarnessEnvironment.loader(fixture);
    TestbedHarnessEnvironment.documentRootLoader(fixture);

    httpTestingController = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should enable and set failover config', async () => {
    const api = fakeApi({
      id: API_ID,
      proxy: {
        failover: undefined,
      },
    });
    expectApiGetRequest(api);
    const saveBar = await loader.getHarness(GioSaveBarHarness);
    expect(await saveBar.isVisible()).toBe(false);

    const enabledSlideToggle = await loader.getHarness(MatSlideToggleHarness.with({ selector: '[formControlName="enabled"]' }));
    expect(await enabledSlideToggle.isChecked()).toEqual(false);

    // Check each field is disabled
    const maxAttemptsInput = await loader.getHarness(MatInputHarness.with({ selector: '[formControlName="maxAttempts"]' }));
    expect(await maxAttemptsInput.isDisabled()).toBe(true);

    const retryTimeoutInput = await loader.getHarness(MatInputHarness.with({ selector: '[formControlName="retryTimeout"]' }));
    expect(await retryTimeoutInput.isDisabled()).toBe(true);

    // Enable Failover & set some values
    await enabledSlideToggle.toggle();

    await maxAttemptsInput.setValue('2');
    await retryTimeoutInput.setValue('22');

    expect(await saveBar.isSubmitButtonInvalid()).toEqual(false);
    await saveBar.clickSubmit();

    // Expect fetch api and update
    expectApiGetRequest(api);
    const req = httpTestingController.expectOne({ method: 'PUT', url: `${CONSTANTS_TESTING.env.baseURL}/apis/${API_ID}` });
    expect(req.request.body.proxy.failover).toStrictEqual({
      maxAttempts: 2,
      retryTimeout: 22,
    });
  });

  it('should update failover config', async () => {
    const api = fakeApi({
      id: API_ID,
      proxy: {
        failover: {
          maxAttempts: 2,
          retryTimeout: 22,
        },
      },
    });
    expectApiGetRequest(api);
    const saveBar = await loader.getHarness(GioSaveBarHarness);

    const enabledSlideToggle = await loader.getHarness(MatSlideToggleHarness.with({ selector: '[formControlName="enabled"]' }));
    expect(await enabledSlideToggle.isChecked()).toEqual(true);

    const maxAttemptsInput = await loader.getHarness(MatInputHarness.with({ selector: '[formControlName="maxAttempts"]' }));
    expect(await maxAttemptsInput.getValue()).toEqual('2');
    await maxAttemptsInput.setValue('3');

    const retryTimeoutInput = await loader.getHarness(MatInputHarness.with({ selector: '[formControlName="retryTimeout"]' }));
    expect(await retryTimeoutInput.getValue()).toEqual('22');
    await retryTimeoutInput.setValue('33');

    expect(await saveBar.isSubmitButtonInvalid()).toEqual(false);
    await saveBar.clickSubmit();

    // Expect fetch api and update
    expectApiGetRequest(api);
    const req = httpTestingController.expectOne({ method: 'PUT', url: `${CONSTANTS_TESTING.env.baseURL}/apis/${API_ID}` });
    expect(req.request.body.proxy.failover).toStrictEqual({
      maxAttempts: 3,
      retryTimeout: 33,
    });
  });

  it('should disable fields when origin is kubernetes', async () => {
    const api = fakeApi({
      id: API_ID,
      proxy: {
        failover: undefined,
      },
      definition_context: { origin: 'kubernetes' },
    });
    expectApiGetRequest(api);

    const saveBar = await loader.getHarness(GioSaveBarHarness);
    expect(await saveBar.isVisible()).toBe(false);

    const maxAttemptsInput = await loader.getHarness(MatInputHarness.with({ selector: '[formControlName="maxAttempts"]' }));
    expect(await maxAttemptsInput.isDisabled()).toBe(true);

    const retryTimeoutInput = await loader.getHarness(MatInputHarness.with({ selector: '[formControlName="retryTimeout"]' }));
    expect(await retryTimeoutInput.isDisabled()).toBe(true);
  });

  function expectApiGetRequest(api: Api) {
    httpTestingController.expectOne({ url: `${CONSTANTS_TESTING.env.baseURL}/apis/${api.id}`, method: 'GET' }).flush(api);
    fixture.detectChanges();
  }
});
