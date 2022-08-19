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
    ServiceV4,
    ServiceV4FromJSON,
    ServiceV4FromJSONTyped,
    ServiceV4ToJSON,
} from './';

/**
 * 
 * @export
 * @interface EndpointGroupServicesV4
 */
export interface EndpointGroupServicesV4 {
    /**
     * 
     * @type {ServiceV4}
     * @memberof EndpointGroupServicesV4
     */
    discovery?: ServiceV4;
    /**
     * 
     * @type {ServiceV4}
     * @memberof EndpointGroupServicesV4
     */
    healthCheck?: ServiceV4;
}

export function EndpointGroupServicesV4FromJSON(json: any): EndpointGroupServicesV4 {
    return EndpointGroupServicesV4FromJSONTyped(json, false);
}

export function EndpointGroupServicesV4FromJSONTyped(json: any, ignoreDiscriminator: boolean): EndpointGroupServicesV4 {
    if ((json === undefined) || (json === null)) {
        return json;
    }
    return {
        
        'discovery': !exists(json, 'discovery') ? undefined : ServiceV4FromJSON(json['discovery']),
        'healthCheck': !exists(json, 'healthCheck') ? undefined : ServiceV4FromJSON(json['healthCheck']),
    };
}

export function EndpointGroupServicesV4ToJSON(value?: EndpointGroupServicesV4 | null): any {
    if (value === undefined) {
        return undefined;
    }
    if (value === null) {
        return null;
    }
    return {
        
        'discovery': ServiceV4ToJSON(value.discovery),
        'healthCheck': ServiceV4ToJSON(value.healthCheck),
    };
}

