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
export var RoleScope;
(function (RoleScope) {
    RoleScope["API"] = "API";
    RoleScope["APPLICATION"] = "APPLICATION";
    RoleScope["GROUP"] = "GROUP";
    RoleScope["ENVIRONMENT"] = "ENVIRONMENT";
    RoleScope["ORGANIZATION"] = "ORGANIZATION";
    RoleScope["PLATFORM"] = "PLATFORM";
})(RoleScope || (RoleScope = {}));
export function RoleScopeFromJSON(json) {
    return RoleScopeFromJSONTyped(json, false);
}
export function RoleScopeFromJSONTyped(json, ignoreDiscriminator) {
    return json;
}
export function RoleScopeToJSON(value) {
    return value;
}