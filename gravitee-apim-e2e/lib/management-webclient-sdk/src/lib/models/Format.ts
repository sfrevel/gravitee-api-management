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
export enum Format {
    WSDL = 'WSDL',
    API = 'API'
}

export function FormatFromJSON(json: any): Format {
    return FormatFromJSONTyped(json, false);
}

export function FormatFromJSONTyped(json: any, ignoreDiscriminator: boolean): Format {
    return json as Format;
}

export function FormatToJSON(value?: Format | null): any {
    return value as any;
}
