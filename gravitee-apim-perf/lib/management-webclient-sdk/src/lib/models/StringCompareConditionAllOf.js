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
export function StringCompareConditionAllOfFromJSON(json) {
    return StringCompareConditionAllOfFromJSONTyped(json, false);
}
export function StringCompareConditionAllOfFromJSONTyped(json, ignoreDiscriminator) {
    if ((json === undefined) || (json === null)) {
        return json;
    }
    return {
        'property': !exists(json, 'property') ? undefined : json['property'],
        'operator': !exists(json, 'operator') ? undefined : json['operator'],
        'property2': !exists(json, 'property2') ? undefined : json['property2'],
        'ignoreCase': !exists(json, 'ignoreCase') ? undefined : json['ignoreCase'],
    };
}
export function StringCompareConditionAllOfToJSON(value) {
    if (value === undefined) {
        return undefined;
    }
    if (value === null) {
        return null;
    }
    return {
        'property': value.property,
        'operator': value.operator,
        'property2': value.property2,
        'ignoreCase': value.ignoreCase,
    };
}
/**
* @export
* @enum {string}
*/
export var StringCompareConditionAllOfOperatorEnum;
(function (StringCompareConditionAllOfOperatorEnum) {
    StringCompareConditionAllOfOperatorEnum["EQUALS"] = "EQUALS";
    StringCompareConditionAllOfOperatorEnum["NOTEQUALS"] = "NOT_EQUALS";
    StringCompareConditionAllOfOperatorEnum["STARTSWITH"] = "STARTS_WITH";
    StringCompareConditionAllOfOperatorEnum["ENDSWITH"] = "ENDS_WITH";
    StringCompareConditionAllOfOperatorEnum["CONTAINS"] = "CONTAINS";
    StringCompareConditionAllOfOperatorEnum["MATCHES"] = "MATCHES";
})(StringCompareConditionAllOfOperatorEnum || (StringCompareConditionAllOfOperatorEnum = {}));