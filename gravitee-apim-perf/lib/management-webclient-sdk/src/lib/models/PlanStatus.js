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
/**
 *
 * @export
 * @enum {string}
 */
export var PlanStatus;
(function (PlanStatus) {
    PlanStatus["STAGING"] = "STAGING";
    PlanStatus["PUBLISHED"] = "PUBLISHED";
    PlanStatus["CLOSED"] = "CLOSED";
    PlanStatus["DEPRECATED"] = "DEPRECATED";
})(PlanStatus || (PlanStatus = {}));
export function PlanStatusFromJSON(json) {
    return PlanStatusFromJSONTyped(json, false);
}
export function PlanStatusFromJSONTyped(json, ignoreDiscriminator) {
    return json;
}
export function PlanStatusToJSON(value) {
    return value;
}