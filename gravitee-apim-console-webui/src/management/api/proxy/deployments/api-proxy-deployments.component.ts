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
import { FormGroup, FormControl } from '@angular/forms';
import { combineLatest, EMPTY, Subject } from 'rxjs';
import { catchError, switchMap, takeUntil, tap } from 'rxjs/operators';

import { UIRouterStateParams } from '../../../../ajs-upgraded-providers';
import { Tag } from '../../../../entities/tag/tag';
import { ApiService } from '../../../../services-ngx/api.service';
import { SnackBarService } from '../../../../services-ngx/snack-bar.service';
import { TagService } from '../../../../services-ngx/tag.service';
import { GioPermissionService } from '../../../../shared/components/gio-permission/gio-permission.service';

@Component({
  selector: 'api-proxy-deployments',
  template: require('./api-proxy-deployments.component.html'),
  styles: [require('./api-proxy-deployments.component.scss')],
})
export class ApiProxyDeploymentsComponent implements OnInit, OnDestroy {
  private unsubscribe$: Subject<boolean> = new Subject<boolean>();

  public shardingTags: Tag[];
  public deploymentsForm: FormGroup;
  public initialDeploymentsFormValue: unknown;

  constructor(
    @Inject(UIRouterStateParams) private readonly ajsStateParams,
    private readonly apiService: ApiService,
    private readonly tagService: TagService,
    private readonly snackBarService: SnackBarService,
    private readonly permissionService: GioPermissionService,
  ) {}

  ngOnInit(): void {
    combineLatest([this.apiService.get(this.ajsStateParams.apiId), this.tagService.list()])
      .pipe(
        takeUntil(this.unsubscribe$),
        tap(([api, shardingTags]) => {
          this.shardingTags = shardingTags;

          const isReadOnly =
            !this.permissionService.hasAnyMatching(['api-definition-u']) || api.definition_context?.origin === 'kubernetes';

          this.deploymentsForm = new FormGroup({
            tags: new FormControl({
              value: api.tags ?? [],
              disabled: isReadOnly,
            }),
          });

          this.initialDeploymentsFormValue = this.deploymentsForm.getRawValue();
        }),
      )
      .subscribe();
  }

  ngOnDestroy() {
    this.unsubscribe$.next(true);
    this.unsubscribe$.unsubscribe();
  }

  onSubmit() {
    return this.apiService
      .get(this.ajsStateParams.apiId)
      .pipe(
        takeUntil(this.unsubscribe$),
        switchMap((api) => this.apiService.update({ ...api, tags: this.deploymentsForm.get('tags').value ?? [] })),
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
