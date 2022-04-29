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
 * @interface MonitoringProcess
 */
export interface MonitoringProcess {
    /**
     * 
     * @type {number}
     * @memberof MonitoringProcess
     */
    cpu_percent?: number;
    /**
     * 
     * @type {number}
     * @memberof MonitoringProcess
     */
    open_file_descriptors?: number;
    /**
     * 
     * @type {number}
     * @memberof MonitoringProcess
     */
    max_file_descriptors?: number;
}

export function MonitoringProcessFromJSON(json: any): MonitoringProcess {
    return MonitoringProcessFromJSONTyped(json, false);
}

export function MonitoringProcessFromJSONTyped(json: any, ignoreDiscriminator: boolean): MonitoringProcess {
    if ((json === undefined) || (json === null)) {
        return json;
    }
    return {
        
        'cpu_percent': !exists(json, 'cpu_percent') ? undefined : json['cpu_percent'],
        'open_file_descriptors': !exists(json, 'open_file_descriptors') ? undefined : json['open_file_descriptors'],
        'max_file_descriptors': !exists(json, 'max_file_descriptors') ? undefined : json['max_file_descriptors'],
    };
}

export function MonitoringProcessToJSON(value?: MonitoringProcess | null): any {
    if (value === undefined) {
        return undefined;
    }
    if (value === null) {
        return null;
    }
    return {
        
        'cpu_percent': value.cpu_percent,
        'open_file_descriptors': value.open_file_descriptors,
        'max_file_descriptors': value.max_file_descriptors,
    };
}

