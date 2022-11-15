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
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { EMPTY, Subject } from 'rxjs';
import { catchError, switchMap, takeUntil, tap } from 'rxjs/operators';

import { UIRouterStateParams } from '../../../../ajs-upgraded-providers';
import { ProxyFailover } from '../../../../entities/proxy';
import { ApiService } from '../../../../services-ngx/api.service';
import { SnackBarService } from '../../../../services-ngx/snack-bar.service';
import { GioPermissionService } from '../../../../shared/components/gio-permission/gio-permission.service';

@Component({
  selector: 'api-proxy-failover',
  template: require('./api-proxy-failover.component.html'),
  styles: [require('./api-proxy-failover.component.scss')],
})
export class ApiProxyFailoverComponent implements OnInit, OnDestroy {
  private unsubscribe$: Subject<boolean> = new Subject<boolean>();

  private failoverForm: FormGroup;
  public initialFailoverFormValue: ProxyFailover;

  public get enabled() {
    return this.failoverForm.get('enabled');
  }

  public get maxAttempts() {
    return this.failoverForm.get('maxAttempts');
  }

  public get retryTimeout() {
    return this.failoverForm.get('retryTimeout');
  }

  constructor(
    private readonly formBuilder: FormBuilder,
    @Inject(UIRouterStateParams) private readonly ajsStateParams,
    private readonly apiService: ApiService,
    private readonly snackBarService: SnackBarService,
    private readonly permissionService: GioPermissionService,
  ) {}

  ngOnInit(): void {
    this.apiService
      .get(this.ajsStateParams.apiId)
      .pipe(
        takeUntil(this.unsubscribe$),
        tap((api) => {
          const isReadOnly =
            !this.permissionService.hasAnyMatching(['api-definition-u']) || api.definition_context?.origin === 'kubernetes';
          this.createForm(isReadOnly, api.proxy?.failover);
          this.setupDisablingFields();
        }),
      )
      .subscribe();
  }

  ngOnDestroy() {
    this.unsubscribe$.next(true);
    this.unsubscribe$.unsubscribe();
  }

  private createForm(isReadOnly: boolean, failover?: ProxyFailover) {
    const isFailoverReadOnly = isReadOnly || !failover;

    this.failoverForm = this.formBuilder.group({
      enabled: [{ value: !!failover, disabled: isReadOnly }, []],
      maxAttempts: [{ value: failover?.maxAttempts ?? null, disabled: isFailoverReadOnly }, [Validators.required]],
      retryTimeout: [{ value: failover?.retryTimeout ?? null, disabled: isFailoverReadOnly }, [Validators.required]],
    });
    this.initialFailoverFormValue = this.failoverForm.getRawValue();
  }

  private setupDisablingFields() {
    const controlKeys = ['maxAttempts', 'retryTimeout'];
    this.enabled.valueChanges.pipe(takeUntil(this.unsubscribe$)).subscribe((checked) => {
      controlKeys.forEach((k) => {
        return checked ? this.failoverForm.get(k).enable() : this.failoverForm.get(k).disable();
      });
    });
  }

  onSubmit() {
    const { enabled, maxAttempts, retryTimeout } = this.failoverForm.getRawValue();
    const failover: ProxyFailover = enabled
      ? {
          maxAttempts,
          retryTimeout,
        }
      : undefined;

    return this.apiService
      .get(this.ajsStateParams.apiId)
      .pipe(
        takeUntil(this.unsubscribe$),
        switchMap(({ proxy, ...api }) =>
          this.apiService.update({
            ...api,
            proxy: {
              ...proxy,
              failover,
            },
          }),
        ),
        tap(() => this.snackBarService.success('Configuration successfully saved!')),
        catchError(({ error }) => {
          this.snackBarService.error(error.message);
          return EMPTY;
        }),
        tap(() => this.ngOnInit()),
      )
      .subscribe();
  }
}
