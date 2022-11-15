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
import { MatDialog } from '@angular/material/dialog';
import { GioConfirmDialogComponent, GioConfirmDialogData } from '@gravitee/ui-particles-angular';
import { get, isEmpty, isNil } from 'lodash';
import { combineLatest, EMPTY, Subject } from 'rxjs';
import { catchError, switchMap, takeUntil, tap } from 'rxjs/operators';

import { UIRouterStateParams } from '../../../../ajs-upgraded-providers';
import { Api } from '../../../../entities/api';
import { ApiService } from '../../../../services-ngx/api.service';
import { EnvironmentService } from '../../../../services-ngx/environment.service';
import { SnackBarService } from '../../../../services-ngx/snack-bar.service';
import { GioPermissionService } from '../../../../shared/components/gio-permission/gio-permission.service';

@Component({
  selector: 'api-proxy-entrypoints',
  template: require('./api-proxy-entrypoints.component.html'),
  styles: [require('./api-proxy-entrypoints.component.scss')],
})
export class ApiProxyEntrypointsComponent implements OnInit, OnDestroy {
  private unsubscribe$: Subject<boolean> = new Subject<boolean>();

  public virtualHostModeEnabled = false;
  public domainRestrictions: string[] = [];

  public apiProxy: Api['proxy'];
  public isReadOnly = false;

  constructor(
    @Inject(UIRouterStateParams) private readonly ajsStateParams,
    private readonly apiService: ApiService,
    private readonly environmentService: EnvironmentService,
    private readonly matDialog: MatDialog,
    private readonly permissionService: GioPermissionService,
    private readonly snackBarService: SnackBarService,
  ) {}

  ngOnInit(): void {
    combineLatest([this.apiService.get(this.ajsStateParams.apiId), this.environmentService.getCurrent()])
      .pipe(
        takeUntil(this.unsubscribe$),
        tap(([api, environment]) => {
          this.apiProxy = api.proxy;

          // virtual host mode is enabled if there domain restrictions or if there is more than one virtual host or if the first virtual host has a host
          this.virtualHostModeEnabled =
            !isEmpty(environment.domainRestrictions) ||
            get(api, 'proxy.virtual_hosts', []) > 1 ||
            !isNil(get(api, 'proxy.virtual_hosts[0].host', null));

          this.domainRestrictions = environment.domainRestrictions ?? [];

          this.isReadOnly =
            !this.permissionService.hasAnyMatching(['api-definition-u', 'api-gateway_definition-u']) ||
            api.definition_context?.origin === 'kubernetes';
        }),
      )
      .subscribe();
  }

  ngOnDestroy() {
    this.unsubscribe$.next(true);
    this.unsubscribe$.unsubscribe();
  }

  onSubmit(apiProxy: Api['proxy']) {
    return this.apiService
      .get(this.ajsStateParams.apiId)
      .pipe(
        takeUntil(this.unsubscribe$),
        switchMap((api) => this.apiService.update({ ...api, proxy: apiProxy })),
        tap((api) => (this.apiProxy = api.proxy)),
        tap(() => this.snackBarService.success('Configuration successfully saved!')),
        catchError(({ error }) => {
          this.snackBarService.error(error.message);
          return EMPTY;
        }),
      )
      .subscribe();
  }

  switchVirtualHostMode() {
    if (this.virtualHostModeEnabled) {
      this.matDialog
        .open<GioConfirmDialogComponent, GioConfirmDialogData, boolean>(GioConfirmDialogComponent, {
          width: '500px',
          data: {
            title: 'Switch to context-path mode',
            content: `By moving back to context-path you will loose all virtual-hosts. Are you sure to continue?`,
            confirmButton: 'Switch',
          },
          role: 'alertdialog',
          id: 'switchContextPathConfirmDialog',
        })
        .afterClosed()
        .pipe(
          takeUntil(this.unsubscribe$),
          tap((response) => {
            if (response) {
              // Keep only the first virtual_host path
              this.onSubmit({
                ...this.apiProxy,
                virtual_hosts: [
                  {
                    path: this.apiProxy.virtual_hosts[0].path,
                  },
                ],
              });
              this.virtualHostModeEnabled = !this.virtualHostModeEnabled;
            }
          }),
        )
        .subscribe();
      return;
    }

    this.virtualHostModeEnabled = !this.virtualHostModeEnabled;
  }
}
