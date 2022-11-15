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
import { Component, Inject, Input, OnDestroy, OnInit } from '@angular/core';
import { StateService } from '@uirouter/core';
import { GioMenuService } from '@gravitee/ui-particles-angular';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { UIRouterState } from '../../../../ajs-upgraded-providers';
import { GioPermissionService } from '../../../../shared/components/gio-permission/gio-permission.service';

interface MenuItem {
  targetRoute?: string;
  baseRoute?: string;
  displayName: string;
  permissions?: string[];
}

@Component({
  selector: 'application-navigation',
  template: require('./application-navigation.component.html'),
  styles: [require('./application-navigation.component.scss')],
})
export class ApplicationNavigationComponent implements OnInit, OnDestroy {
  @Input()
  public applicationName: string;
  public subMenuItems: MenuItem[] = [];
  public hasTitle = true;
  private unsubscribe$ = new Subject();

  constructor(
    @Inject(UIRouterState) private readonly ajsState: StateService,
    private readonly permissionService: GioPermissionService,
    private readonly gioMenuService: GioMenuService,
  ) {}

  ngOnInit() {
    this.gioMenuService.reduce.pipe(takeUntil(this.unsubscribe$)).subscribe((reduced) => {
      this.hasTitle = !reduced;
    });
    this.subMenuItems = this.filterMenuByPermission([
      {
        displayName: 'Global settings',
        targetRoute: 'management.applications.application.general',
        baseRoute: 'management.applications.application.general',
        permissions: ['application-definition-r'],
      },
      {
        displayName: 'Metadata',
        targetRoute: 'management.applications.application.metadata',
        baseRoute: 'management.applications.application.metadata',
        permissions: ['application-metadata-r'],
      },
      {
        displayName: 'Subscriptions',
        targetRoute: 'management.applications.application.subscriptions.list',
        baseRoute: 'management.applications.application.subscriptions',
        permissions: ['application-subscription-r'],
      },
      {
        displayName: 'Members',
        targetRoute: 'management.applications.application.members',
        baseRoute: 'management.applications.application.members',
        permissions: ['application-member-r'],
      },
      {
        displayName: 'Analytics',
        targetRoute: 'management.applications.application.analytics',
        baseRoute: 'management.applications.application.analytics',
        permissions: ['application-analytics-r'],
      },
      {
        displayName: 'Logs',
        targetRoute: 'management.applications.application.logs.list',
        baseRoute: 'management.applications.application.logs',
        permissions: ['application-log-r'],
      },
      {
        displayName: 'Notifications',
        targetRoute: 'management.applications.application.notifications',
        baseRoute: 'management.applications.application.notifications',
        permissions: ['application-notification-r', 'application-alert-r'],
      },
    ]);
  }

  ngOnDestroy(): void {
    this.unsubscribe$.next();
    this.unsubscribe$.unsubscribe();
  }

  private filterMenuByPermission(menuItems: MenuItem[]): MenuItem[] {
    if (menuItems) {
      return menuItems.filter((item) => !item.permissions || this.permissionService.hasAnyMatching(item.permissions));
    }
    return [];
  }

  navigateTo(route: string) {
    this.ajsState.go(route);
  }

  isActive(route: string): boolean {
    return this.ajsState.includes(route);
  }
}
