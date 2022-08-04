/* tslint:disable */
/* eslint-disable */
/**
 * Gravitee.io - Management API
 * No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
 *
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

import { exists, mapValues } from '../runtime';
import {
    ApplicationRequestItem,
    ApplicationRequestItemFromJSON,
    ApplicationRequestItemFromJSONTyped,
    ApplicationRequestItemToJSON,
} from './';

/**
 * 
 * @export
 * @interface ApplicationRequestItemSearchLogResponse
 */
export interface ApplicationRequestItemSearchLogResponse {
    /**
     * 
     * @type {Array<ApplicationRequestItem>}
     * @memberof ApplicationRequestItemSearchLogResponse
     */
    logs?: Array<ApplicationRequestItem>;
    /**
     * 
     * @type {{ [key: string]: { [key: string]: string; }; }}
     * @memberof ApplicationRequestItemSearchLogResponse
     */
    metadata?: { [key: string]: { [key: string]: string; }; };
    /**
     * 
     * @type {number}
     * @memberof ApplicationRequestItemSearchLogResponse
     */
    total?: number;
}

export function ApplicationRequestItemSearchLogResponseFromJSON(json: any): ApplicationRequestItemSearchLogResponse {
    return ApplicationRequestItemSearchLogResponseFromJSONTyped(json, false);
}

export function ApplicationRequestItemSearchLogResponseFromJSONTyped(json: any, ignoreDiscriminator: boolean): ApplicationRequestItemSearchLogResponse {
    if ((json === undefined) || (json === null)) {
        return json;
    }
    return {
        
        'logs': !exists(json, 'logs') ? undefined : ((json['logs'] as Array<any>).map(ApplicationRequestItemFromJSON)),
        'metadata': !exists(json, 'metadata') ? undefined : json['metadata'],
        'total': !exists(json, 'total') ? undefined : json['total'],
    };
}

export function ApplicationRequestItemSearchLogResponseToJSON(value?: ApplicationRequestItemSearchLogResponse | null): any {
    if (value === undefined) {
        return undefined;
    }
    if (value === null) {
        return null;
    }
    return {
        
        'logs': value.logs === undefined ? undefined : ((value.logs as Array<any>).map(ApplicationRequestItemToJSON)),
        'metadata': value.metadata,
        'total': value.total,
    };
}

