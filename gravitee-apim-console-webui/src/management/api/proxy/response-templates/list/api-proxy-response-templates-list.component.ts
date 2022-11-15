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
import { StateService } from '@uirouter/angular';
import { EMPTY, of, Subject } from 'rxjs';
import { catchError, filter, switchMap, takeUntil, tap } from 'rxjs/operators';

import { UIRouterState, UIRouterStateParams } from '../../../../../ajs-upgraded-providers';
import { ApiService } from '../../../../../services-ngx/api.service';
import { SnackBarService } from '../../../../../services-ngx/snack-bar.service';
import { GioPermissionService } from '../../../../../shared/components/gio-permission/gio-permission.service';
import { ResponseTemplate, toResponseTemplates } from '../response-templates.adapter';

@Component({
  selector: 'api-proxy-response-templates-list',
  template: require('./api-proxy-response-templates-list.component.html'),
  styles: [require('./api-proxy-response-templates-list.component.scss')],
})
export class ApiProxyResponseTemplatesListComponent implements OnInit, OnDestroy {
  private unsubscribe$: Subject<boolean> = new Subject<boolean>();

  public responseTemplateTableDisplayedColumns = ['key', 'contentType', 'statusCode', 'actions'];
  public responseTemplateTableData: ResponseTemplate[];
  public isReadOnly = false;
  public apiId: string;

  constructor(
    @Inject(UIRouterStateParams) private readonly ajsStateParams,
    @Inject(UIRouterState) private readonly ajsState: StateService,
    private readonly apiService: ApiService,
    private readonly permissionService: GioPermissionService,
    private readonly matDialog: MatDialog,
    private readonly snackBarService: SnackBarService,
  ) {}

  ngOnInit(): void {
    this.apiService
      .get(this.ajsStateParams.apiId)
      .pipe(
        takeUntil(this.unsubscribe$),
        tap((api) => {
          this.apiId = api.id;
          this.responseTemplateTableData = toResponseTemplates(api.response_templates);

          this.isReadOnly =
            !this.permissionService.hasAnyMatching(['api-response_templates-u']) || api.definition_context?.origin === 'kubernetes';
        }),
      )
      .subscribe();
  }

  ngOnDestroy() {
    this.unsubscribe$.next(true);
    this.unsubscribe$.unsubscribe();
  }

  onAddResponseTemplateClicked() {
    this.ajsState.go('management.apis.detail.proxy.ng-responsetemplates.new', { apiId: this.apiId });
  }

  onEditResponseTemplateClicked(element: ResponseTemplate) {
    this.ajsState.go('management.apis.detail.proxy.ng-responsetemplates.edit', { apiId: this.apiId, responseTemplateId: element.id });
  }

  onDeleteResponseTemplateClicked(element: ResponseTemplate) {
    this.matDialog
      .open<GioConfirmDialogComponent, GioConfirmDialogData>(GioConfirmDialogComponent, {
        width: '500px',
        data: {
          title: 'Delete a Response Template',
          content: `Are you sure you want to delete the Response Template <strong>${element.key} - ${element.contentType}</strong>?`,
          confirmButton: 'Delete',
        },
        role: 'alertdialog',
        id: 'deleteResponseTemplateConfirmDialog',
      })
      .afterClosed()
      .pipe(
        takeUntil(this.unsubscribe$),
        filter((confirm) => confirm === true),
        switchMap(() => this.apiService.get(this.ajsStateParams.apiId)),
        switchMap((api) => {
          if (api.response_templates[element.key] && api.response_templates[element.key][element.contentType]) {
            delete api.response_templates[element.key][element.contentType];
            return this.apiService.update(api);
          }
          return of({});
        }),
        catchError(({ error }) => {
          this.snackBarService.error(error.message);
          return EMPTY;
        }),
        tap(() => this.snackBarService.success(`Response Template ${element.key} - ${element.contentType} successfully deleted!`)),
      )
      .subscribe(() => this.ngOnInit());
  }
}
