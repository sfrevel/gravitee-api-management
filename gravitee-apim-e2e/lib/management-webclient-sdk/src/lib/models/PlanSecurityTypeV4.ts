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
 */
export const PlanSecurityTypeV4 = {
    KEY_LESS: 'key-less',
    API_KEY: 'api-key',
    OAUTH2: 'oauth2',
    JWT: 'jwt'
} as const;
export type PlanSecurityTypeV4 = typeof PlanSecurityTypeV4[keyof typeof PlanSecurityTypeV4];


export function PlanSecurityTypeV4FromJSON(json: any): PlanSecurityTypeV4 {
    return PlanSecurityTypeV4FromJSONTyped(json, false);
}

export function PlanSecurityTypeV4FromJSONTyped(json: any, ignoreDiscriminator: boolean): PlanSecurityTypeV4 {
    return json as PlanSecurityTypeV4;
}

export function PlanSecurityTypeV4ToJSON(value?: PlanSecurityTypeV4 | null): any {
    return value as any;
}
