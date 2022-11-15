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
import { formatDate } from '@angular/common';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { UIRouterStateParams } from '../../../../ajs-upgraded-providers';
import { Instance } from '../../../../entities/instance/instance';
import { InstanceService } from '../../../../services-ngx/instance.service';
import { GioTableWrapperFilters } from '../../../../shared/components/gio-table-wrapper/gio-table-wrapper.component';
import { gioTableFilterCollection } from '../../../../shared/components/gio-table-wrapper/gio-table-wrapper.util';

type InformationItemDS = {
  icon: string;
  type: string;
  value: string;
  class?: string;
}[];

type PluginItemDS = {
  icon: string;
  id: string;
  name: string;
  version: string;
}[];

type SystemPropertyItemDS = {
  name: string;
  value: string;
}[];

@Component({
  selector: 'instance-details-environment',
  template: require('./instance-details-environment.component.html'),
  styles: [require('./instance-details-environment.component.scss')],
})
export class InstanceDetailsEnvironmentComponent implements OnInit, OnDestroy {
  public instance: Instance;
  public hasSystemProperties = false;

  public informationItemsDS: InformationItemDS;
  public filteredInformationItemsDS: InformationItemDS;
  public informationTableDisplayedColumns = ['icon', 'type', 'value'];
  public informationTableUnpaginatedLength: number;

  public pluginsItemsDS: PluginItemDS;
  public filteredPluginsItemsDS: PluginItemDS;
  public pluginsTableDisplayedColumns = ['icon', 'id', 'name', 'version'];
  public pluginsTableUnpaginatedLength: number;

  public propertiesItemsDS: SystemPropertyItemDS;
  public filteredPropertiesItemsDS: SystemPropertyItemDS;
  public propertiesTableDisplayedColumns = ['name', 'value'];
  public propertiesTableUnpaginatedLength: number;
  private unsubscribe$ = new Subject<void>();

  constructor(@Inject(UIRouterStateParams) private readonly ajsStateParams, private readonly instanceService: InstanceService) {}

  ngOnInit(): void {
    this.instanceService
      .get(this.ajsStateParams.instanceId)
      .pipe(takeUntil(this.unsubscribe$))
      .subscribe((instance) => {
        this.instance = instance;

        this.hasSystemProperties = this.instance.systemProperties ? Object.keys(this.instance.systemProperties).length > 0 : false;

        this.initInformationTable();
        this.initPluginsTable();
        this.initPropertiesTable();
      });
  }

  ngOnDestroy() {
    this.unsubscribe$.next();
    this.unsubscribe$.unsubscribe();
  }

  private initInformationTable() {
    this.informationItemsDS = [
      {
        icon: 'gio:computer',
        type: 'Hostname',
        value: this.instance.hostname,
      },
      {
        icon: 'gio:wifi',
        type: 'IP',
        value: this.instance.ip,
      },
      {
        icon: 'gio:wifi',
        type: 'Port',
        value: this.instance.port,
      },
      {
        icon: this.getIconFromState(this.instance.state),
        type: 'State',
        value: this.instance.state,
        class: this.getClassFromState(this.instance.state),
      },
      {
        icon: 'gio:flag',
        type: 'Version',
        value: this.instance.version,
      },
      {
        icon: 'gio:clock-outline',
        type: 'Started at',
        value: formatDate(this.instance.started_at, 'medium', 'en-US'),
      },
      {
        icon: 'gio:heart',
        type: 'Last heartbeat at',
        value: formatDate(this.instance.last_heartbeat_at, 'medium', 'en-US'),
      },
    ];

    if (this.instance.tags?.length > 0) {
      this.informationItemsDS.push({
        icon: 'gio:label-outline',
        type: 'Sharding tags',
        value: this.instance.tags.join(', '),
      });
    }

    if (this.instance.tenants?.length > 0) {
      this.informationItemsDS.push({
        icon: 'gio:shuffle',
        type: 'Tenant',
        value: this.instance.tenants.join(', '),
      });
    }

    if (this.instance.organizations_hrids?.length > 0) {
      this.informationItemsDS.push({
        icon: 'gio:product-apim',
        type: 'Organizations',
        value: this.instance.organizations_hrids.join(', '),
      });
    }

    if (this.instance.environments_hrids?.length > 0) {
      this.informationItemsDS.push({
        icon: 'gio:server',
        type: 'Environments',
        value: this.instance.environments_hrids.join(', '),
      });
    }

    if (this.instance.stopped_at) {
      this.informationItemsDS.push({
        icon: 'gio:power',
        type: 'Stopped at',
        value: formatDate(this.instance.stopped_at, 'medium', 'en-US'),
      });
    }

    this.filteredInformationItemsDS = this.informationItemsDS;
    this.informationTableUnpaginatedLength = this.filteredInformationItemsDS.length;
  }

  private getIconFromState(state: string): string {
    if (state === 'STARTED') {
      return 'gio:play-circle';
    }
    if (state === 'STOPPED') {
      return 'gio:stop-circle';
    }
  }

  private getClassFromState(state: string): string {
    if (state === 'STARTED') {
      return 'gio-instance-details-environment__started';
    }
    if (state === 'STOPPED') {
      return 'gio-instance-details-environment__stopped';
    }
  }

  private initPluginsTable() {
    const pluginIcon = {
      policy: 'gio:shuffle',
      service: 'gio:layers',
      service_discovery: 'gio:layers',
      repository: 'gio:folder',
      reporter: 'gio:report-columns',
      resource: 'gio:package',
      connector: 'gio:server-connection',
      tracer: 'gio:pen-tool',
      'endpoint-connector': 'gio:server-connection',
      'entrypoint-connector': 'gio:server-connection',
      alert: 'gio:shield-alert',
    };

    this.pluginsItemsDS = this.instance.plugins?.map((plugin) => {
      const pluginItem = {
        id: plugin.id,
        icon: pluginIcon[plugin.type],
        name: plugin.name,
        version: plugin.version,
      };
      return pluginItem;
    });
    this.filteredPluginsItemsDS = this.pluginsItemsDS;
    this.pluginsTableUnpaginatedLength = this.filteredPluginsItemsDS?.length;
  }

  private initPropertiesTable() {
    if (this.hasSystemProperties) {
      this.propertiesItemsDS = Object.entries(this.instance.systemProperties).map(([name, value]) => {
        const propertyItem = { name, value };
        return propertyItem;
      });
      this.filteredPropertiesItemsDS = this.propertiesItemsDS;
      this.propertiesTableUnpaginatedLength = this.filteredPropertiesItemsDS.length;
    }
  }

  onInformationFiltersChanged(filters: GioTableWrapperFilters) {
    const filtered = gioTableFilterCollection(this.informationItemsDS, filters);
    this.filteredInformationItemsDS = filtered.filteredCollection;
    this.informationTableUnpaginatedLength = filtered.unpaginatedLength;
  }

  onPluginsFiltersChanged(filters: GioTableWrapperFilters) {
    const filtered = gioTableFilterCollection(this.pluginsItemsDS, filters);
    this.filteredPluginsItemsDS = filtered.filteredCollection;
    this.pluginsTableUnpaginatedLength = filtered.unpaginatedLength;
  }

  onPropertiesFiltersChanged(filters: GioTableWrapperFilters) {
    const filtered = gioTableFilterCollection(this.propertiesItemsDS, filters);
    this.filteredPropertiesItemsDS = filtered.filteredCollection;
    this.propertiesTableUnpaginatedLength = filtered.unpaginatedLength;
  }
}
