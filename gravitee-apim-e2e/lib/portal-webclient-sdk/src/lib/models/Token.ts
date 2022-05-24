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
/**
 * 
 * @export
 * @interface Token
 */
export interface Token {
    /**
     * 
     * @type {string}
     * @memberof Token
     */
    token_type?: TokenTokenTypeEnum;
    /**
     * 
     * @type {string}
     * @memberof Token
     */
    token?: string;
    /**
     * 
     * @type {string}
     * @memberof Token
     */
    state?: string;
    /**
     * 
     * @type {string}
     * @memberof Token
     */
    access_token?: string;
    /**
     * 
     * @type {string}
     * @memberof Token
     */
    id_token?: string;
}

export function TokenFromJSON(json: any): Token {
    return TokenFromJSONTyped(json, false);
}

export function TokenFromJSONTyped(json: any, ignoreDiscriminator: boolean): Token {
    if ((json === undefined) || (json === null)) {
        return json;
    }
    return {
        
        'token_type': !exists(json, 'token_type') ? undefined : json['token_type'],
        'token': !exists(json, 'token') ? undefined : json['token'],
        'state': !exists(json, 'state') ? undefined : json['state'],
        'access_token': !exists(json, 'access_token') ? undefined : json['access_token'],
        'id_token': !exists(json, 'id_token') ? undefined : json['id_token'],
    };
}

export function TokenToJSON(value?: Token | null): any {
    if (value === undefined) {
        return undefined;
    }
    if (value === null) {
        return null;
    }
    return {
        
        'token_type': value.token_type,
        'token': value.token,
        'state': value.state,
        'access_token': value.access_token,
        'id_token': value.id_token,
    };
}

/**
* @export
* @enum {string}
*/
export enum TokenTokenTypeEnum {
    BEARER = 'BEARER'
}

