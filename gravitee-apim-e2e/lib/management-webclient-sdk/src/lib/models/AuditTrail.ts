/* tslint:disable */
/* eslint-disable */
/**
 * Gravitee.io - Management API
 * No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
 *
 * The version of the OpenAPI document: 3.18.0-SNAPSHOT
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

import { exists, mapValues } from '../runtime';
/**
 * 
 * @export
 * @interface AuditTrail
 */
export interface AuditTrail {
    /**
     * 
     * @type {boolean}
     * @memberof AuditTrail
     */
    enabled?: boolean;
}

export function AuditTrailFromJSON(json: any): AuditTrail {
    return AuditTrailFromJSONTyped(json, false);
}

export function AuditTrailFromJSONTyped(json: any, ignoreDiscriminator: boolean): AuditTrail {
    if ((json === undefined) || (json === null)) {
        return json;
    }
    return {
        
        'enabled': !exists(json, 'enabled') ? undefined : json['enabled'],
    };
}

export function AuditTrailToJSON(value?: AuditTrail | null): any {
    if (value === undefined) {
        return undefined;
    }
    if (value === null) {
        return null;
    }
    return {
        
        'enabled': value.enabled,
    };
}

