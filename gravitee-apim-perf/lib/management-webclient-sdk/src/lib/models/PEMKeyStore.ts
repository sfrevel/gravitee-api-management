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
    KeyStore,
    KeyStoreFromJSON,
    KeyStoreFromJSONTyped,
    KeyStoreToJSON,
    PEMKeyStoreAllOf,
    PEMKeyStoreAllOfFromJSON,
    PEMKeyStoreAllOfFromJSONTyped,
    PEMKeyStoreAllOfToJSON,
} from './';

/**
 * 
 * @export
 * @interface PEMKeyStore
 */
export interface PEMKeyStore extends KeyStore {
    /**
     * 
     * @type {string}
     * @memberof PEMKeyStore
     */
    keyPath?: string;
    /**
     * 
     * @type {string}
     * @memberof PEMKeyStore
     */
    keyContent?: string;
    /**
     * 
     * @type {string}
     * @memberof PEMKeyStore
     */
    certPath?: string;
    /**
     * 
     * @type {string}
     * @memberof PEMKeyStore
     */
    certContent?: string;
}

export function PEMKeyStoreFromJSON(json: any): PEMKeyStore {
    return PEMKeyStoreFromJSONTyped(json, false);
}

export function PEMKeyStoreFromJSONTyped(json: any, ignoreDiscriminator: boolean): PEMKeyStore {
    if ((json === undefined) || (json === null)) {
        return json;
    }
    return {
        ...KeyStoreFromJSONTyped(json, ignoreDiscriminator),
        'keyPath': !exists(json, 'keyPath') ? undefined : json['keyPath'],
        'keyContent': !exists(json, 'keyContent') ? undefined : json['keyContent'],
        'certPath': !exists(json, 'certPath') ? undefined : json['certPath'],
        'certContent': !exists(json, 'certContent') ? undefined : json['certContent'],
    };
}

export function PEMKeyStoreToJSON(value?: PEMKeyStore | null): any {
    if (value === undefined) {
        return undefined;
    }
    if (value === null) {
        return null;
    }
    return {
        ...KeyStoreToJSON(value),
        'keyPath': value.keyPath,
        'keyContent': value.keyContent,
        'certPath': value.certPath,
        'certContent': value.certContent,
    };
}


