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
    MetadataFormat,
    MetadataFormatFromJSON,
    MetadataFormatFromJSONTyped,
    MetadataFormatToJSON,
} from './';

/**
 * 
 * @export
 * @interface MetadataEntity
 */
export interface MetadataEntity {
    /**
     * 
     * @type {MetadataFormat}
     * @memberof MetadataEntity
     */
    format?: MetadataFormat;
    /**
     * 
     * @type {string}
     * @memberof MetadataEntity
     */
    key?: string;
    /**
     * 
     * @type {string}
     * @memberof MetadataEntity
     */
    name: string;
    /**
     * 
     * @type {string}
     * @memberof MetadataEntity
     */
    value?: string;
}

export function MetadataEntityFromJSON(json: any): MetadataEntity {
    return MetadataEntityFromJSONTyped(json, false);
}

export function MetadataEntityFromJSONTyped(json: any, ignoreDiscriminator: boolean): MetadataEntity {
    if ((json === undefined) || (json === null)) {
        return json;
    }
    return {
        
        'format': !exists(json, 'format') ? undefined : MetadataFormatFromJSON(json['format']),
        'key': !exists(json, 'key') ? undefined : json['key'],
        'name': json['name'],
        'value': !exists(json, 'value') ? undefined : json['value'],
    };
}

export function MetadataEntityToJSON(value?: MetadataEntity | null): any {
    if (value === undefined) {
        return undefined;
    }
    if (value === null) {
        return null;
    }
    return {
        
        'format': MetadataFormatToJSON(value.format),
        'key': value.key,
        'name': value.name,
        'value': value.value,
    };
}

