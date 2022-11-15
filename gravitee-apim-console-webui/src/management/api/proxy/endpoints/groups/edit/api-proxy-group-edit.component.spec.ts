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
import { MatIconTestingModule } from '@angular/material/icon/testing';
import { TestbedHarnessEnvironment } from '@angular/cdk/testing/testbed';
import { HttpTestingController } from '@angular/common/http/testing';
import { HarnessLoader } from '@angular/cdk/testing';
import { MatButtonHarness } from '@angular/material/button/testing';
import { MatInputHarness } from '@angular/material/input/testing';
import { GioSaveBarHarness } from '@gravitee/ui-particles-angular';
import { MatSelectHarness } from '@angular/material/select/testing';
import { MatTabHarness } from '@angular/material/tabs/testing';
import { MatSlideToggleHarness } from '@angular/material/slide-toggle/testing';

import { ApiProxyGroupEditComponent } from './api-proxy-group-edit.component';

import { CONSTANTS_TESTING, GioHttpTestingModule } from '../../../../../../shared/testing';
import { CurrentUserService, UIRouterState, UIRouterStateParams } from '../../../../../../ajs-upgraded-providers';
import { ApiProxyGroupsModule } from '../api-proxy-groups.module';
import { fakeApi } from '../../../../../../entities/api/Api.fixture';
import { Api } from '../../../../../../entities/api';
import { SnackBarService } from '../../../../../../services-ngx/snack-bar.service';
import { fakeConnectorListItem } from '../../../../../../entities/connector/connector-list-item.fixture';
import { ConnectorListItem } from '../../../../../../entities/connector/connector-list-item';
import { ResourceListItem } from '../../../../../../entities/resource/resourceListItem';
import { fakeResourceListItem } from '../../../../../../entities/resource/resourceListItem.fixture';
import { User } from '../../../../../../entities/user';

describe('ApiProxyGroupEditComponent', () => {
  const API_ID = 'apiId';
  const DEFAULT_GROUP_NAME = 'default-group';
  const fakeUiRouter = { go: jest.fn() };

  let fixture: ComponentFixture<ApiProxyGroupEditComponent>;
  let loader: HarnessLoader;
  let httpTestingController: HttpTestingController;
  let connector: ConnectorListItem;
  let serviceDiscovery: ResourceListItem;

  const currentUser = new User();
  currentUser.userPermissions = ['api-definition-u'];

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [NoopAnimationsModule, GioHttpTestingModule, ApiProxyGroupsModule, MatIconTestingModule],
      providers: [
        { provide: UIRouterStateParams, useValue: { apiId: API_ID, groupName: DEFAULT_GROUP_NAME } },
        { provide: UIRouterState, useValue: fakeUiRouter },
        { provide: CurrentUserService, useValue: { currentUser } },
      ],
    });

    connector = fakeConnectorListItem();
    serviceDiscovery = fakeResourceListItem({
      name: 'Consul.io Service Discovery',
      id: 'consul-service-discovery',
    });
  });

  afterEach(() => {
    jest.clearAllMocks();
    httpTestingController.verify();
  });

  describe('Edit mode', () => {
    beforeEach(async () => {
      TestBed.compileComponents();
      fixture = TestBed.createComponent(ApiProxyGroupEditComponent);
      loader = TestbedHarnessEnvironment.loader(fixture);
      httpTestingController = TestBed.inject(HttpTestingController);
      fixture.detectChanges();
    });

    it('should go back to endpoints', async () => {
      const api = fakeApi({
        id: API_ID,
      });
      expectApiGetRequest(api);
      expectConnectorRequest(connector);
      expectServiceDiscoveryRequest(serviceDiscovery);

      const routerSpy = jest.spyOn(fakeUiRouter, 'go');

      await loader.getHarness(MatButtonHarness.with({ selector: '[mattooltip="Go back"]' })).then((button) => button.click());

      expect(routerSpy).toHaveBeenCalledWith('management.apis.detail.proxy.endpoints', { apiId: API_ID }, undefined);
    });

    describe('Edit general information of existing group', () => {
      let api: Api;

      beforeEach(async () => {
        api = fakeApi({
          id: API_ID,
          proxy: {
            groups: [
              {
                name: DEFAULT_GROUP_NAME,
                endpoints: [],
                load_balancing: { type: 'ROUND_ROBIN' },
              },
            ],
          },
        });
        expectApiGetRequest(api);
        expectConnectorRequest(connector);
        expectServiceDiscoveryRequest(serviceDiscovery);

        await loader.getHarness(MatTabHarness.with({ label: 'General' })).then((tab) => tab.select());
        fixture.detectChanges();
      });

      it('should edit group name and save it', async () => {
        const nameInput = await loader.getHarness(MatInputHarness.with({ selector: '[aria-label="Group name input"]' }));
        expect(await nameInput.getValue()).toEqual(DEFAULT_GROUP_NAME);

        const newGroupName = 'new-group-name';
        await nameInput.setValue(newGroupName);
        expect(await nameInput.getValue()).toEqual(newGroupName);

        const gioSaveBar = await loader.getHarness(GioSaveBarHarness);
        expect(await gioSaveBar.isSubmitButtonInvalid()).toBeFalsy();
        await gioSaveBar.clickSubmit();

        expectApiGetRequest(api);
        expectApiPutRequest({
          ...api,
          proxy: {
            groups: [
              {
                name: newGroupName,
                endpoints: [],
                load_balancing: { type: 'ROUND_ROBIN' },
              },
            ],
          },
        });
      });

      it('should not be able to save group when name is invalid', async () => {
        const nameInput = await loader.getHarness(MatInputHarness.with({ selector: '[aria-label="Group name input"]' }));
        expect(await nameInput.getValue()).toEqual(DEFAULT_GROUP_NAME);

        const newGroupName = 'new-group-name : ';
        await nameInput.setValue(newGroupName);
        expect(await nameInput.getValue()).toEqual(newGroupName);

        const gioSaveBar = await loader.getHarness(GioSaveBarHarness);
        expect(await gioSaveBar.isSubmitButtonInvalid()).toBeTruthy();
      });

      it('should update load balancing type and save it', async () => {
        const lbSelect = await loader.getHarness(MatSelectHarness.with({ selector: '[aria-label="Load balancing algorithm"]' }));
        expect(await lbSelect.getValueText()).toEqual('ROUND_ROBIN');

        const newLbType = 'RANDOM';
        await lbSelect.clickOptions({ text: newLbType });
        expect(await lbSelect.getValueText()).toEqual(newLbType);

        const gioSaveBar = await loader.getHarness(GioSaveBarHarness);
        expect(await gioSaveBar.isSubmitButtonInvalid()).toBeFalsy();
        await gioSaveBar.clickSubmit();

        expectApiGetRequest(api);
        expectApiPutRequest({
          ...api,
          proxy: {
            groups: [
              {
                name: DEFAULT_GROUP_NAME,
                endpoints: [],
                load_balancing: { type: newLbType },
              },
            ],
          },
        });
      });

      it('should call snack bar error', async () => {
        const snackBarSpy = jest.spyOn(TestBed.inject(SnackBarService), 'error');
        const newGroupName = 'new-group-name';
        const nameInput = await loader.getHarness(MatInputHarness.with({ selector: '[aria-label="Group name input"]' }));
        await nameInput.setValue(newGroupName);

        const gioSaveBar = await loader.getHarness(GioSaveBarHarness);
        await gioSaveBar.clickSubmit();

        expectApiGetRequest(api);
        expectApiPutRequestError(API_ID);
        expect(snackBarSpy).toHaveBeenCalled();
      });
    });

    describe('Edit configuration of existing group', () => {
      let api: Api;

      beforeEach(async () => {
        api = fakeApi({
          id: API_ID,
        });
        expectApiGetRequest(api);
        expectConnectorRequest(connector);
        expectServiceDiscoveryRequest(serviceDiscovery);
      });

      it('should mark form as invalid, touched and dirty', async () => {
        const component = fixture.componentInstance;
        expect(component.groupForm.valid).toStrictEqual(true);
        expect(component.groupForm.touched).toStrictEqual(false);
        expect(component.groupForm.dirty).toStrictEqual(false);

        component.onConfigurationChange({
          isSchemaValid: false,
          configuration: {},
        });

        expect(component.groupForm.valid).toStrictEqual(false);
        expect(component.groupForm.touched).toStrictEqual(true);
        expect(component.groupForm.dirty).toStrictEqual(true);
      });

      it('should mark unset configuration error', async () => {
        const component = fixture.componentInstance;
        component.onConfigurationChange({
          isSchemaValid: false,
          configuration: {},
        });

        expect(component.groupForm.valid).toStrictEqual(false);
        expect(component.groupForm.touched).toStrictEqual(true);
        expect(component.groupForm.dirty).toStrictEqual(true);

        component.onConfigurationChange({
          isSchemaValid: true,
          configuration: {},
        });
        expect(component.groupForm.valid).toStrictEqual(true);
      });

      it('should update api configuration', async () => {
        const component = fixture.componentInstance;
        component.onConfigurationChange({
          isSchemaValid: false,
          configuration: {
            http: {
              ...api.proxy.groups[0].http,
              connectTimeout: 1000,
            },
          },
        });

        component.onSubmit();
        expectApiGetRequest(api);
        expectApiPutRequest({
          ...api,
          proxy: {
            groups: [
              {
                ...api.proxy.groups[0],
                http: {
                  ...api.proxy.groups[0].http,
                  connectTimeout: 1000,
                },
              },
            ],
          },
        });
        expect(component.api.proxy.groups[0].http.connectTimeout).toStrictEqual(1000);
      });
    });

    describe('Edit existing service discovery configuration', () => {
      let api: Api;

      beforeEach(async () => {
        api = fakeApi({
          id: API_ID,
        });
        expectApiGetRequest(api);
        expectConnectorRequest(connector);
        expectServiceDiscoveryRequest(serviceDiscovery);

        await loader.getHarness(MatTabHarness.with({ label: 'Service discovery' })).then((tab) => tab.select());

        expect(fixture.debugElement.nativeElement.querySelector('#sdSchemaForm')).toBeFalsy();

        await loader.getHarness(MatSlideToggleHarness.with({ selector: '[formControlName="enabled"]' })).then((slide) => slide.toggle());

        await loader
          .getHarness(MatSelectHarness.with({ selector: '[aria-label="Service discovery type"]' }))
          .then((select) => select.clickOptions({ text: 'Consul.io Service Discovery' }));

        fixture.detectChanges();
        expectServiceDiscoverySchemaRequest();
      });

      it('should display service discovery gv-schema-form and save url', async () => {
        expect(fixture.debugElement.nativeElement.querySelector('gv-schema-form')).toBeTruthy();

        await loader.getHarness(GioSaveBarHarness).then((saveBar) => saveBar.clickSubmit());

        expectApiGetRequest(api);
        expectApiPutRequest(api);
      });

      it('should disable select', async () => {
        await loader.getHarness(MatSlideToggleHarness.with({ selector: '[formControlName="enabled"]' })).then((slide) => slide.toggle());

        expect(
          await loader
            .getHarness(MatSelectHarness.with({ selector: '[aria-label="Service discovery type"]' }))
            .then((select) => select.isDisabled()),
        ).toEqual(true);
      });

      it('should mark the form as invalid when the service discovery is enabled but the url is not set', async () => {
        const component = fixture.componentInstance;
        component.onServiceDiscoveryChange({
          isSchemaValid: false,
          serviceDiscoveryValues: {},
        });

        expect(component.groupForm.valid).toStrictEqual(false);
        expect(await loader.getHarness(GioSaveBarHarness).then((saveBar) => saveBar.isSubmitButtonInvalid())).toEqual(true);
      });
    });

    describe('Reset', () => {
      let api: Api;

      beforeEach(async () => {
        api = fakeApi({
          id: API_ID,
        });
        expectApiGetRequest(api);
        expectConnectorRequest(connector);
        expectServiceDiscoveryRequest(serviceDiscovery);
      });

      it('should reset the forms', async () => {
        const newGroupName = 'new-group-name';
        const nameInput = await loader.getHarness(MatInputHarness.with({ selector: '[aria-label="Group name input"]' }));
        await nameInput.setValue(newGroupName);
        expect(await nameInput.getValue()).toStrictEqual(newGroupName);

        const newLbType = 'RANDOM';
        const lbSelect = await loader.getHarness(MatSelectHarness.with({ selector: '[aria-label="Load balancing algorithm"]' }));
        await lbSelect.clickOptions({ text: newLbType });
        expect(await lbSelect.getValueText()).toEqual(newLbType);
        fixture.detectChanges();

        const gioSaveBar = await loader.getHarness(GioSaveBarHarness);
        await gioSaveBar.clickReset();

        expectApiGetRequest(api);
        expectConnectorRequest(connector);
        expectServiceDiscoveryRequest(serviceDiscovery);
        expect(await nameInput.getValue()).toStrictEqual('default-group');
        expect(await lbSelect.getValueText()).toStrictEqual('ROUND_ROBIN');
      });
    });
  });

  describe('New mode ', () => {
    let api: Api;

    beforeEach(() => {
      TestBed.overrideProvider(UIRouterStateParams, { useValue: { apiId: API_ID, groupName: null } });
      TestBed.compileComponents();
      fixture = TestBed.createComponent(ApiProxyGroupEditComponent);
      loader = TestbedHarnessEnvironment.loader(fixture);
      httpTestingController = TestBed.inject(HttpTestingController);
      fixture.detectChanges();

      api = fakeApi({
        id: API_ID,
      });
      expectApiGetRequest(api);
      expectConnectorRequest(connector);
      expectServiceDiscoveryRequest(serviceDiscovery);
    });

    it('should create new group', async () => {
      const nameInput = await loader.getHarness(MatInputHarness.with({ selector: '[aria-label="Group name input"]' }));
      expect(await nameInput.getValue()).toStrictEqual('');

      const newGroupName = 'new-group-name';
      await nameInput.setValue(newGroupName);
      expect(await nameInput.getValue()).toStrictEqual(newGroupName);

      const newLbType = 'RANDOM';
      const lbSelect = await loader.getHarness(MatSelectHarness.with({ selector: '[aria-label="Load balancing algorithm"]' }));
      await lbSelect.clickOptions({ text: newLbType });
      expect(await lbSelect.getValueText()).toEqual(newLbType);
      fixture.detectChanges();

      const gioSaveBar = await loader.getHarness(GioSaveBarHarness);
      expect(await gioSaveBar.isSubmitButtonInvalid()).toBeFalsy();
      await gioSaveBar.clickSubmit();

      expectApiGetRequest(api);
      expectApiPutRequest({
        ...api,
        proxy: {
          groups: [
            {
              name: newGroupName,
              endpoints: [],
              load_balancing: { type: 'RANDOM' },
            },
          ],
        },
      });
    });

    it('should not be able to create new group when name is already used', async () => {
      const newGroupName = 'default-group';
      await loader
        .getHarness(MatInputHarness.with({ selector: '[aria-label="Group name input"]' }))
        .then((inputName) => inputName.setValue(newGroupName));
      fixture.detectChanges();

      const gioSaveBar = await loader.getHarness(GioSaveBarHarness);
      expect(await gioSaveBar.isSubmitButtonInvalid()).toBeTruthy();
      expect(fixture.componentInstance.generalForm.get('name').hasError('isUnique')).toBeTruthy();
    });
  });

  describe('Read only', () => {
    let api: Api;

    beforeEach(() => {
      TestBed.overrideProvider(UIRouterStateParams, { useValue: { apiId: API_ID, groupName: null } });
      TestBed.compileComponents();
      fixture = TestBed.createComponent(ApiProxyGroupEditComponent);
      loader = TestbedHarnessEnvironment.loader(fixture);
      httpTestingController = TestBed.inject(HttpTestingController);
      fixture.detectChanges();

      api = fakeApi({
        id: API_ID,
        definition_context: {
          origin: 'kubernetes',
        },
      });
      expectApiGetRequest(api);
      expectConnectorRequest(connector);
      expectServiceDiscoveryRequest(serviceDiscovery);
    });

    it('should not allow user to update the form', async () => {
      expect(
        await loader
          .getHarness(MatInputHarness.with({ selector: '[aria-label="Group name input"]' }))
          .then((nameInput) => nameInput.isDisabled()),
      ).toBeTruthy();

      expect(
        await loader
          .getHarness(MatSelectHarness.with({ selector: '[aria-label="Load balancing algorithm"]' }))
          .then((select) => select.isDisabled()),
      ).toBeTruthy();

      await loader.getHarness(MatTabHarness.with({ label: 'Service discovery' })).then((tab) => tab.select());

      expect(
        await loader
          .getHarness(MatSlideToggleHarness.with({ selector: '[formControlName="enabled"]' }))
          .then((slide) => slide.isDisabled()),
      ).toBeTruthy();

      expect(
        await loader
          .getHarness(MatSelectHarness.with({ selector: '[aria-label="Service discovery type"]' }))
          .then((select) => select.isDisabled()),
      ).toBeTruthy();
    });
  });

  function expectApiGetRequest(api: Api) {
    httpTestingController.expectOne({ url: `${CONSTANTS_TESTING.env.baseURL}/apis/${api.id}`, method: 'GET' }).flush(api);
    fixture.detectChanges();
  }

  function expectConnectorRequest(connector: ConnectorListItem) {
    httpTestingController.expectOne({ url: `${CONSTANTS_TESTING.env.baseURL}/connectors?expand=schema`, method: 'GET' }).flush([connector]);
    fixture.detectChanges();
  }

  function expectServiceDiscoveryRequest(serviceDiscovery: ResourceListItem) {
    httpTestingController
      .expectOne({ url: `${CONSTANTS_TESTING.env.baseURL}/services-discovery`, method: 'GET' })
      .flush([serviceDiscovery]);
    fixture.detectChanges();
  }

  function expectServiceDiscoverySchemaRequest() {
    httpTestingController
      .expectOne({ url: `${CONSTANTS_TESTING.env.baseURL}/services-discovery/consul-service-discovery/schema`, method: 'GET' })
      .flush({
        id: 'urn:jsonschema:io:gravitee:discovery:consul:configuration:ConsulServiceDiscoveryConfiguration',
        properties: {
          url: {
            title: 'Consul.io URL',
            description: 'Address of the Consul.io agent',
            type: 'string',
            default: 'http://localhost:8500',
          },
          required: ['url', 'service', 'trustStoreType', 'keyStoreType'],
          type: 'object',
        },
      });
    fixture.detectChanges();
  }

  function expectApiPutRequest(api: Api) {
    httpTestingController.expectOne({ url: `${CONSTANTS_TESTING.env.baseURL}/apis/${api.id}`, method: 'PUT' }).flush(api);
    fixture.detectChanges();
  }

  function expectApiPutRequestError(apiId: string) {
    httpTestingController
      .expectOne({ url: `${CONSTANTS_TESTING.env.baseURL}/apis/${apiId}`, method: 'PUT' })
      .error(new ErrorEvent('error'));
    fixture.detectChanges();
  }
});
