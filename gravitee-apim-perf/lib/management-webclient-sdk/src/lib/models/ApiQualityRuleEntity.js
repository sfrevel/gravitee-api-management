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
export function ApiQualityRuleEntityFromJSON(json) {
    return ApiQualityRuleEntityFromJSONTyped(json, false);
}
export function ApiQualityRuleEntityFromJSONTyped(json, ignoreDiscriminator) {
    if ((json === undefined) || (json === null)) {
        return json;
    }
    return {
        'api': !exists(json, 'api') ? undefined : json['api'],
        'checked': !exists(json, 'checked') ? undefined : json['checked'],
        'created_at': !exists(json, 'created_at') ? undefined : (new Date(json['created_at'])),
        'quality_rule': !exists(json, 'quality_rule') ? undefined : json['quality_rule'],
        'updated_at': !exists(json, 'updated_at') ? undefined : (new Date(json['updated_at'])),
    };
}
export function ApiQualityRuleEntityToJSON(value) {
    if (value === undefined) {
        return undefined;
    }
    if (value === null) {
        return null;
    }
    return {
        'api': value.api,
        'checked': value.checked,
        'created_at': value.created_at === undefined ? undefined : (value.created_at.toISOString()),
        'quality_rule': value.quality_rule,
        'updated_at': value.updated_at === undefined ? undefined : (value.updated_at.toISOString()),
    };
}