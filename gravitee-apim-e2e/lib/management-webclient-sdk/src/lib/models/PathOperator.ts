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
 * @interface PathOperator
 */
export interface PathOperator {
    /**
     * 
     * @type {string}
     * @memberof PathOperator
     */
    path?: string;
    /**
     * 
     * @type {string}
     * @memberof PathOperator
     */
    operator?: PathOperatorOperatorEnum;
}

export function PathOperatorFromJSON(json: any): PathOperator {
    return PathOperatorFromJSONTyped(json, false);
}

export function PathOperatorFromJSONTyped(json: any, ignoreDiscriminator: boolean): PathOperator {
    if ((json === undefined) || (json === null)) {
        return json;
    }
    return {
        
        'path': !exists(json, 'path') ? undefined : json['path'],
        'operator': !exists(json, 'operator') ? undefined : json['operator'],
    };
}

export function PathOperatorToJSON(value?: PathOperator | null): any {
    if (value === undefined) {
        return undefined;
    }
    if (value === null) {
        return null;
    }
    return {
        
        'path': value.path,
        'operator': value.operator,
    };
}

/**
* @export
* @enum {string}
*/
export enum PathOperatorOperatorEnum {
    STARTSWITH = 'STARTS_WITH',
    EQUALS = 'EQUALS'
}

