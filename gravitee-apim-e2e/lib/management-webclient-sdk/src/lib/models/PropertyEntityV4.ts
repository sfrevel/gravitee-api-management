/* tslint:disable */
/* eslint-disable */
/**
 * Gravitee.io - Management API
 * Some news resources are in alpha version. This implies that they are likely to be modified or even removed in future versions. They are marked with the 🧪 symbol
 *
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

import { exists, mapValues } from '../runtime';
/**
 * A dictionary (could be dynamic) of properties available in the API context.
 * @export
 * @interface PropertyEntityV4
 */
export interface PropertyEntityV4 {
    /**
     * 
     * @type {boolean}
     * @memberof PropertyEntityV4
     */
    dynamic?: boolean;
    /**
     * 
     * @type {boolean}
     * @memberof PropertyEntityV4
     */
    encryptable?: boolean;
    /**
     * 
     * @type {boolean}
     * @memberof PropertyEntityV4
     */
    encrypted?: boolean;
    /**
     * 
     * @type {string}
     * @memberof PropertyEntityV4
     */
    key: string;
    /**
     * 
     * @type {string}
     * @memberof PropertyEntityV4
     */
    value: string;
}

/**
 * Check if a given object implements the PropertyEntityV4 interface.
 */
export function instanceOfPropertyEntityV4(value: object): boolean {
    let isInstance = true;
    isInstance = isInstance && "key" in value;
    isInstance = isInstance && "value" in value;

    return isInstance;
}

export function PropertyEntityV4FromJSON(json: any): PropertyEntityV4 {
    return PropertyEntityV4FromJSONTyped(json, false);
}

export function PropertyEntityV4FromJSONTyped(json: any, ignoreDiscriminator: boolean): PropertyEntityV4 {
    if ((json === undefined) || (json === null)) {
        return json;
    }
    return {
        
        'dynamic': !exists(json, 'dynamic') ? undefined : json['dynamic'],
        'encryptable': !exists(json, 'encryptable') ? undefined : json['encryptable'],
        'encrypted': !exists(json, 'encrypted') ? undefined : json['encrypted'],
        'key': json['key'],
        'value': json['value'],
    };
}

export function PropertyEntityV4ToJSON(value?: PropertyEntityV4 | null): any {
    if (value === undefined) {
        return undefined;
    }
    if (value === null) {
        return null;
    }
    return {
        
        'dynamic': value.dynamic,
        'encryptable': value.encryptable,
        'encrypted': value.encrypted,
        'key': value.key,
        'value': value.value,
    };
}
