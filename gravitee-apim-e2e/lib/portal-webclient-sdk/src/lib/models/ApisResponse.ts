/* tslint:disable */
/* eslint-disable */
/**
 * Gravitee.io Portal Rest API
 * API dedicated to the devportal part of Gravitee
 *
 * Contact: contact@graviteesource.com
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

import { exists, mapValues } from '../runtime';
import {
    Api,
    ApiFromJSON,
    ApiFromJSONTyped,
    ApiToJSON,
    Links,
    LinksFromJSON,
    LinksFromJSONTyped,
    LinksToJSON,
} from './';

/**
 * 
 * @export
 * @interface ApisResponse
 */
export interface ApisResponse {
    /**
     * List of API.
     * @type {Array<Api>}
     * @memberof ApisResponse
     */
    data?: Array<Api>;
    /**
     * Map of Map of Object
     * @type {{ [key: string]: { [key: string]: any; }; }}
     * @memberof ApisResponse
     */
    metadata?: { [key: string]: { [key: string]: any; }; };
    /**
     * 
     * @type {Links}
     * @memberof ApisResponse
     */
    links?: Links;
}

export function ApisResponseFromJSON(json: any): ApisResponse {
    return ApisResponseFromJSONTyped(json, false);
}

export function ApisResponseFromJSONTyped(json: any, ignoreDiscriminator: boolean): ApisResponse {
    if ((json === undefined) || (json === null)) {
        return json;
    }
    return {
        
        'data': !exists(json, 'data') ? undefined : ((json['data'] as Array<any>).map(ApiFromJSON)),
        'metadata': !exists(json, 'metadata') ? undefined : json['metadata'],
        'links': !exists(json, 'links') ? undefined : LinksFromJSON(json['links']),
    };
}

export function ApisResponseToJSON(value?: ApisResponse | null): any {
    if (value === undefined) {
        return undefined;
    }
    if (value === null) {
        return null;
    }
    return {
        
        'data': value.data === undefined ? undefined : ((value.data as Array<any>).map(ApiToJSON)),
        'metadata': value.metadata,
        'links': LinksToJSON(value.links),
    };
}

