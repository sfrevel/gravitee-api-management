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
import { exists } from '../runtime';
export function CustomUserFieldEntityFromJSON(json) {
    return CustomUserFieldEntityFromJSONTyped(json, false);
}
export function CustomUserFieldEntityFromJSONTyped(json, ignoreDiscriminator) {
    if ((json === undefined) || (json === null)) {
        return json;
    }
    return {
        'key': json['key'],
        'label': json['label'],
        'required': !exists(json, 'required') ? undefined : json['required'],
        'values': !exists(json, 'values') ? undefined : json['values'],
    };
}
export function CustomUserFieldEntityToJSON(value) {
    if (value === undefined) {
        return undefined;
    }
    if (value === null) {
        return null;
    }
    return {
        'key': value.key,
        'label': value.label,
        'required': value.required,
        'values': value.values,
    };
}