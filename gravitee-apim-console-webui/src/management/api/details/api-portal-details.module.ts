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

import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { GioFormFilePickerModule, GioFormTagsInputModule, GioSaveBarModule } from '@gravitee/ui-particles-angular';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';

import { ApiPortalDetailsComponent } from './api-portal-details.component';

import { GioAvatarModule } from '../../../shared/components/gio-avatar/gio-avatar.module';
import { GioFormFocusInvalidModule } from '../../../shared/components/gio-form-focus-first-invalid/gio-form-focus-first-invalid.module';
import { GioClipboardModule } from '../../../shared/components/gio-clipboard/gio-clipboard.module';

@NgModule({
  declarations: [ApiPortalDetailsComponent],
  exports: [ApiPortalDetailsComponent],
  imports: [
    CommonModule,
    ReactiveFormsModule,

    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSnackBarModule,
    MatIconModule,
    MatSelectModule,

    GioFormFocusInvalidModule,
    GioAvatarModule,
    GioFormFilePickerModule,
    GioSaveBarModule,
    GioFormTagsInputModule,
    GioClipboardModule,
  ],
})
export class ApiPortalDetailsModule {}