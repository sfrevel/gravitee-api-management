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
import {
    IdentityProviderType,
    IdentityProviderTypeFromJSON,
    IdentityProviderTypeFromJSONTyped,
    IdentityProviderTypeToJSON,
} from './';

/**
 * 
 * @export
 * @interface IdentityProviderListItem
 */
export interface IdentityProviderListItem {
    /**
     * 
     * @type {string}
     * @memberof IdentityProviderListItem
     */
    id?: string;
    /**
     * 
     * @type {string}
     * @memberof IdentityProviderListItem
     */
    name?: string;
    /**
     * 
     * @type {string}
     * @memberof IdentityProviderListItem
     */
    description?: string;
    /**
     * 
     * @type {boolean}
     * @memberof IdentityProviderListItem
     */
    enabled?: boolean;
    /**
     * 
     * @type {boolean}
     * @memberof IdentityProviderListItem
     */
    sync?: boolean;
    /**
     * 
     * @type {IdentityProviderType}
     * @memberof IdentityProviderListItem
     */
    type?: IdentityProviderType;
    /**
     * 
     * @type {Date}
     * @memberof IdentityProviderListItem
     */
    created_at?: Date;
    /**
     * 
     * @type {Date}
     * @memberof IdentityProviderListItem
     */
    updated_at?: Date;
}

export function IdentityProviderListItemFromJSON(json: any): IdentityProviderListItem {
    return IdentityProviderListItemFromJSONTyped(json, false);
}

export function IdentityProviderListItemFromJSONTyped(json: any, ignoreDiscriminator: boolean): IdentityProviderListItem {
    if ((json === undefined) || (json === null)) {
        return json;
    }
    return {
        
        'id': !exists(json, 'id') ? undefined : json['id'],
        'name': !exists(json, 'name') ? undefined : json['name'],
        'description': !exists(json, 'description') ? undefined : json['description'],
        'enabled': !exists(json, 'enabled') ? undefined : json['enabled'],
        'sync': !exists(json, 'sync') ? undefined : json['sync'],
        'type': !exists(json, 'type') ? undefined : IdentityProviderTypeFromJSON(json['type']),
        'created_at': !exists(json, 'created_at') ? undefined : (new Date(json['created_at'])),
        'updated_at': !exists(json, 'updated_at') ? undefined : (new Date(json['updated_at'])),
    };
}

export function IdentityProviderListItemToJSON(value?: IdentityProviderListItem | null): any {
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
        'enabled': value.enabled,
        'sync': value.sync,
        'type': IdentityProviderTypeToJSON(value.type),
        'created_at': value.created_at === undefined ? undefined : (value.created_at.toISOString()),
        'updated_at': value.updated_at === undefined ? undefined : (value.updated_at.toISOString()),
    };
}

