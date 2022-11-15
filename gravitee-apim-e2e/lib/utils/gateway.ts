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
import 'dotenv/config';
import fetchApi, { HeadersInit, Response } from 'node-fetch';

import { fetchEventSource } from './eventsource-fetch';

export type HttpMethod = 'GET' | 'PUT' | 'POST' | 'DELETE' | 'OPTIONS';

interface GatewayRequest {
  contextPath: string;
  expectedStatusCode: number;
  expectedResponseValidator: (response: Response) => boolean | Promise<boolean>; // Allows to validate if the expected request is the right one. Useful in case of api redeployment.
  method: HttpMethod;
  body?: string;
  headers?: HeadersInit;
  timeBetweenRetries: number;
  maxRetries: number;
}

export interface Logger {
  error(...data: any[]): void;
  info(...data: any[]): void;
}

export async function fetchGatewaySuccess(request?: Partial<GatewayRequest>, logger: Logger = console) {
  return _fetchGatewayWithRetries({ expectedStatusCode: 200, ...request }, logger);
}

export async function fetchGatewayUnauthorized(request?: Partial<GatewayRequest>, logger: Logger = console) {
  return _fetchGatewayWithRetries({ expectedStatusCode: 401, ...request }, logger);
}

export async function fetchGatewayBadRequest(request?: Partial<GatewayRequest>, logger: Logger = console) {
  return _fetchGatewayWithRetries({ expectedStatusCode: 400, ...request }, logger);
}

export async function fetchGatewayServiceUnavailable(request?: Partial<GatewayRequest>, logger: Logger = console) {
  return _fetchGatewayWithRetries({ expectedStatusCode: 503, ...request }, logger);
}

async function _fetchGatewayWithRetries(attributes: Partial<GatewayRequest>, logger: Logger): Promise<Response> {
  const request = <GatewayRequest>{
    expectedStatusCode: 200,
    method: 'GET',
    timeBetweenRetries: 1500,
    maxRetries: 5,
    expectedResponseValidator: () => true,
    ...attributes,
  };

  if (request.maxRetries <= 0) {
    return await _fetchGateway(request);
  }

  let lastError: Error;

  for (let retries = request.maxRetries; retries > 0; --retries) {
    try {
      return await _fetchGateway(request);
    } catch (error) {
      // logger.info(error);
      lastError = error;
      if (retries > 0) {
        logger.info(`Retrying in ${request.timeBetweenRetries} ms with ${retries} attempts`);
        await sleep(request.timeBetweenRetries);
      }
    }
  }

  logger.info(
    `[${request.method}] [${process.env.GATEWAY_BASE_URL}${request.contextPath}] failed after ${request.maxRetries} retries with error`,
    lastError,
  );

  throw lastError;
}

async function _fetchGateway(request: Partial<GatewayRequest>): Promise<Response> {
  const response = await fetchApi(`${process.env.GATEWAY_BASE_URL}${request.contextPath}`, {
    method: request.method,
    body: request.body,
    headers: request.headers,
  });

  if (response.status != request.expectedStatusCode) {
    throw new Error(`[${request.method}] [${process.env.GATEWAY_BASE_URL}${request.contextPath}] returned HTTP ${response.status}`);
  }

  const isValidResponse = await request.expectedResponseValidator(response);

  if (!isValidResponse) {
    throw new Error(`Unexpected response for [${request.method}] [${process.env.GATEWAY_BASE_URL}${request.contextPath}]`);
  }

  return response;
}

export async function fetchEventSourceGateway(request: Partial<GatewayRequest>, onmessage, logger = console): Promise<unknown> {
  return await fetchEventSource(`${process.env.GATEWAY_BASE_URL}${request.contextPath}`, {
    onmessage,
    timeBetweenRetries: 1500,
    maxRetries: 5,
  });
}

export function sleep(ms: number) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}
