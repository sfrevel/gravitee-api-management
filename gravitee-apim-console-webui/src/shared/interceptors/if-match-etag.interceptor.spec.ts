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
import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HttpClient, HTTP_INTERCEPTORS } from '@angular/common/http';

import { IfMatchEtagInterceptor } from './if-match-etag.interceptor';

describe('IfMatchEtagInterceptor', () => {
  let httpClient: HttpClient;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [{ provide: HTTP_INTERCEPTORS, useClass: IfMatchEtagInterceptor, multi: true }],
    });

    httpClient = TestBed.inject(HttpClient);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should set if-match with last etag for api urls', () => {
    const testUrl = 'https://test.com/management/organizations/DEFAULT/environments/DEFAULT/apis/f35505ff-ac3f-45d6-b134-e4406e8e0165';

    let expectedSteps = 0;
    // First request with etag
    httpClient.get<unknown>(testUrl).subscribe(() => {
      expectedSteps++;
    });
    httpTestingController.expectOne(testUrl).flush('', { headers: { etag: '1234' } });

    // Put request with if-match
    httpClient.put<unknown>(testUrl, '').subscribe(() => {
      expectedSteps++;
    });
    const req = httpTestingController.expectOne(testUrl);
    expect(req.request.headers.get('if-match')).toEqual('1234');
    req.flush('', { headers: { etag: '5678' } });

    // Post request with if-match
    httpClient.post<unknown>(testUrl, '').subscribe(() => {
      expectedSteps++;
    });
    const req2 = httpTestingController.expectOne(testUrl);
    expect(req2.request.headers.get('if-match')).toEqual('5678');
    req2.flush('', { headers: { etag: '9012' } });

    expect(expectedSteps).toEqual(3);
  });
});
