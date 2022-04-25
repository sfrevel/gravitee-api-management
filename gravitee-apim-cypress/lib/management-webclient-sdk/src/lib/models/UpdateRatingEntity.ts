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
 * @interface UpdateRatingEntity
 */
export interface UpdateRatingEntity {
    /**
     * 
     * @type {string}
     * @memberof UpdateRatingEntity
     */
    id?: string;
    /**
     * 
     * @type {string}
     * @memberof UpdateRatingEntity
     */
    api?: string;
    /**
     * 
     * @type {string}
     * @memberof UpdateRatingEntity
     */
    rate?: string;
    /**
     * 
     * @type {string}
     * @memberof UpdateRatingEntity
     */
    title?: string;
    /**
     * 
     * @type {string}
     * @memberof UpdateRatingEntity
     */
    comment?: string;
}

export function UpdateRatingEntityFromJSON(json: any): UpdateRatingEntity {
    return UpdateRatingEntityFromJSONTyped(json, false);
}

export function UpdateRatingEntityFromJSONTyped(json: any, ignoreDiscriminator: boolean): UpdateRatingEntity {
    if ((json === undefined) || (json === null)) {
        return json;
    }
    return {
        
        'id': !exists(json, 'id') ? undefined : json['id'],
        'api': !exists(json, 'api') ? undefined : json['api'],
        'rate': !exists(json, 'rate') ? undefined : json['rate'],
        'title': !exists(json, 'title') ? undefined : json['title'],
        'comment': !exists(json, 'comment') ? undefined : json['comment'],
    };
}

export function UpdateRatingEntityToJSON(value?: UpdateRatingEntity | null): any {
    if (value === undefined) {
        return undefined;
    }
    if (value === null) {
        return null;
    }
    return {
        
        'id': value.id,
        'api': value.api,
        'rate': value.rate,
        'title': value.title,
        'comment': value.comment,
    };
}

