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
 * @interface FetcherListItem
 */
export interface FetcherListItem {
    /**
     * 
     * @type {string}
     * @memberof FetcherListItem
     */
    id?: string;
    /**
     * 
     * @type {string}
     * @memberof FetcherListItem
     */
    name?: string;
    /**
     * 
     * @type {string}
     * @memberof FetcherListItem
     */
    description?: string;
    /**
     * 
     * @type {string}
     * @memberof FetcherListItem
     */
    version?: string;
    /**
     * 
     * @type {string}
     * @memberof FetcherListItem
     */
    schema?: string;
}

export function FetcherListItemFromJSON(json: any): FetcherListItem {
    return FetcherListItemFromJSONTyped(json, false);
}

export function FetcherListItemFromJSONTyped(json: any, ignoreDiscriminator: boolean): FetcherListItem {
    if ((json === undefined) || (json === null)) {
        return json;
    }
    return {
        
        'id': !exists(json, 'id') ? undefined : json['id'],
        'name': !exists(json, 'name') ? undefined : json['name'],
        'description': !exists(json, 'description') ? undefined : json['description'],
        'version': !exists(json, 'version') ? undefined : json['version'],
        'schema': !exists(json, 'schema') ? undefined : json['schema'],
    };
}

export function FetcherListItemToJSON(value?: FetcherListItem | null): any {
    if (value === undefined) {
        return undefined;
    }
    if (value === null) {
        return null;
    }
    return {
        
        'id': value.id,
        'name': value.name,
        'description': value.description,
        'version': value.version,
        'schema': value.schema,
    };
}

