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
import { Component, EventEmitter, Input, Output } from '@angular/core';

import { ProxyGroup } from '../../../../../../../entities/proxy';
import { ConfigurationEvent, SchemaFormEvent } from '../../api-proxy-groups.model';

@Component({
  selector: 'api-proxy-group-configuration',
  template: require('./api-proxy-group-configuration.component.html'),
  styles: [require('./api-proxy-group-configuration.component.scss')],
})
export class ApiProxyGroupConfigurationComponent {
  @Input() schemaForm: unknown;
  @Input() group: ProxyGroup;
  @Input() isReadOnly: boolean;
  @Output() onConfigurationChange = new EventEmitter<ConfigurationEvent>();

  public onChange(event: SchemaFormEvent): void {
    this.onConfigurationChange.emit({
      isSchemaValid: !event.detail?.validation?.errors?.length,
      configuration: event.detail?.values,
    });
  }
}
