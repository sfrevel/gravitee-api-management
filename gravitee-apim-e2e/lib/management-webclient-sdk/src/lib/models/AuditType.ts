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
export enum AuditType {
    ORGANIZATION = 'ORGANIZATION',
    ENVIRONMENT = 'ENVIRONMENT',
    APPLICATION = 'APPLICATION',
    API = 'API'
}

export function AuditTypeFromJSON(json: any): AuditType {
    return AuditTypeFromJSONTyped(json, false);
}

export function AuditTypeFromJSONTyped(json: any, ignoreDiscriminator: boolean): AuditType {
    return json as AuditType;
}

export function AuditTypeToJSON(value?: AuditType | null): any {
    return value as any;
}
