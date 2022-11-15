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
import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { catchError, map, switchMap, takeUntil, tap } from 'rxjs/operators';
import { combineLatest, EMPTY, Subject, Subscription } from 'rxjs';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { StateService } from '@uirouter/core';

import { UIRouterState, UIRouterStateParams } from '../../../../../../../ajs-upgraded-providers';
import { ConnectorService } from '../../../../../../../services-ngx/connector.service';
import { ApiService } from '../../../../../../../services-ngx/api.service';
import { Api } from '../../../../../../../entities/api';
import { ProxyConfiguration, ProxyGroupEndpoint } from '../../../../../../../entities/proxy';
import { TenantService } from '../../../../../../../services-ngx/tenant.service';
import { Tenant } from '../../../../../../../entities/tenant/tenant';
import { SnackBarService } from '../../../../../../../services-ngx/snack-bar.service';
import { toProxyGroupEndpoint } from '../api-proxy-group-endpoint.adapter';
import { isUniq } from '../../edit/api-proxy-group-edit.validator';
import { ConnectorListItem } from '../../../../../../../entities/connector/connector-list-item';
import { ConfigurationEvent } from '../../api-proxy-groups.model';
import { GioPermissionService } from '../../../../../../../shared/components/gio-permission/gio-permission.service';

@Component({
  selector: 'api-proxy-group-endpoint-edit',
  template: require('./api-proxy-group-endpoint-edit.component.html'),
  styles: [require('./api-proxy-group-endpoint-edit.component.scss')],
})
export class ApiProxyGroupEndpointEditComponent implements OnInit, OnDestroy {
  private unsubscribe$: Subject<boolean> = new Subject<boolean>();
  private api: Api;
  private connectors: ConnectorListItem[];
  private updatedConfiguration: ProxyConfiguration;
  private mode: 'edit' | 'new';

  public apiId: string;
  public isReadOnly: boolean;
  public supportedTypes: string[];
  public endpointForm: FormGroup;
  public generalForm: FormGroup;
  public configurationForm: FormGroup;
  public endpoint: ProxyGroupEndpoint;
  public initialEndpointFormValue: unknown;
  public tenants: Tenant[];
  public configurationSchema: unknown;

  constructor(
    @Inject(UIRouterStateParams) private readonly ajsStateParams,
    @Inject(UIRouterState) private readonly ajsState: StateService,
    private readonly formBuilder: FormBuilder,
    private readonly apiService: ApiService,
    private readonly connectorService: ConnectorService,
    private readonly tenantService: TenantService,
    private readonly snackBarService: SnackBarService,
    private readonly permissionService: GioPermissionService,
  ) {}

  public ngOnInit(): void {
    this.apiId = this.ajsStateParams.apiId;
    this.mode = !this.ajsStateParams.groupName || !this.ajsStateParams.endpointName ? 'new' : 'edit';

    combineLatest([this.apiService.get(this.apiId), this.connectorService.list(true), this.tenantService.list()])
      .pipe(
        takeUntil(this.unsubscribe$),
        map(([api, connectors, tenants]) => {
          this.api = api;
          this.connectors = connectors;
          this.tenants = tenants;
          this.isReadOnly = !this.permissionService.hasAnyMatching(['api-definition-u']) || api.definition_context?.origin === 'kubernetes';
          this.initForms();
          this.supportedTypes = this.connectors.map((connector) => connector.supportedTypes).reduce((acc, val) => acc.concat(val), []);
          this.configurationSchema = JSON.parse(
            this.connectors.find((connector) => connector.supportedTypes.includes(this.endpoint?.type?.toLowerCase() ?? 'http'))?.schema,
          );
        }),
      )
      .subscribe();
  }

  public ngOnDestroy(): void {
    this.unsubscribe$.next(true);
    this.unsubscribe$.complete();
  }

  public onSubmit(): Subscription {
    return this.apiService
      .get(this.apiId)
      .pipe(
        takeUntil(this.unsubscribe$),
        switchMap((api) => {
          const groupIndex = api.proxy.groups.findIndex((group) => group.name === this.ajsStateParams.groupName);

          let endpointIndex = -1;
          if (this.mode === 'edit') {
            endpointIndex = api.proxy.groups[groupIndex].endpoints.findIndex(
              (endpoint) => endpoint.name === this.ajsStateParams.endpointName,
            );
          }

          const updatedEndpoint = toProxyGroupEndpoint(
            api.proxy.groups[groupIndex]?.endpoints[endpointIndex],
            this.generalForm.getRawValue(),
            {
              ...this.updatedConfiguration,
              inherit: this.configurationForm.getRawValue().inherit,
            },
          );

          endpointIndex !== -1
            ? api.proxy.groups[groupIndex].endpoints.splice(endpointIndex, 1, updatedEndpoint)
            : api.proxy.groups[groupIndex].endpoints.push(updatedEndpoint);

          return this.apiService.update(api);
        }),
        tap(() => this.snackBarService.success('Configuration successfully saved!')),
        catchError(({ error }) => {
          this.snackBarService.error(error.message);
          return EMPTY;
        }),
        tap(() => this.ajsState.go('management.apis.detail.proxy.endpoints', { apiId: this.apiId })),
      )
      .subscribe();
  }

  public onConfigurationChange(event: ConfigurationEvent) {
    this.endpointForm.markAsDirty();
    this.endpointForm.markAsTouched();
    if (this.endpointForm.getError('invalidConfiguration') && event.isSchemaValid) {
      delete this.endpointForm.errors['invalidConfiguration'];
      this.endpointForm.updateValueAndValidity();
    } else if (!event.isSchemaValid) {
      this.endpointForm.setErrors({ invalidConfiguration: true });
    }
    this.updatedConfiguration = event.configuration;
  }

  private initForms(): void {
    const group = this.api.proxy.groups.find((group) => group.name === this.ajsStateParams.groupName);

    if (group && group.endpoints && group.endpoints.length > 0) {
      this.endpoint = {
        ...group.endpoints.find((endpoint) => endpoint.name === this.ajsStateParams.endpointName),
      };
    }

    this.generalForm = this.formBuilder.group({
      name: [
        {
          value: this.endpoint?.name ?? null,
          disabled: this.isReadOnly,
        },
        [
          Validators.required,
          Validators.pattern(/^[^:]*$/),
          isUniq(
            group.endpoints.reduce((acc, endpoint) => [...acc, endpoint.name], []),
            this.endpoint?.name,
          ),
        ],
      ],
      type: [{ value: this.endpoint?.type ?? 'http', disabled: this.isReadOnly }, [Validators.required]],
      target: [{ value: this.endpoint?.target ?? null, disabled: this.isReadOnly }, [Validators.required]],
      weight: [{ value: this.endpoint?.weight ?? null, disabled: this.isReadOnly }, [Validators.required]],
      tenants: [{ value: this.endpoint?.tenants ?? null, disabled: this.isReadOnly }],
      backup: [{ value: this.endpoint?.backup ?? false, disabled: this.isReadOnly }],
    });

    this.configurationForm = this.formBuilder.group({
      inherit: [{ value: this.endpoint?.inherit ?? true, disabled: this.isReadOnly }],
    });

    this.endpointForm = this.formBuilder.group({
      general: this.generalForm,
      configuration: this.configurationForm,
    });

    this.initialEndpointFormValue = this.endpointForm.getRawValue();

    this.generalForm
      .get('type')
      .valueChanges.pipe(takeUntil(this.unsubscribe$))
      .subscribe((type) => {
        this.configurationSchema = JSON.parse(this.connectors.find((connector) => connector.supportedTypes.includes(type))?.schema);
      });
  }
}
