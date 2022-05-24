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
export enum PageType {
    ASCIIDOC = 'ASCIIDOC',
    ASYNCAPI = 'ASYNCAPI',
    MARKDOWN = 'MARKDOWN',
    MARKDOWNTEMPLATE = 'MARKDOWN_TEMPLATE',
    SWAGGER = 'SWAGGER',
    FOLDER = 'FOLDER',
    LINK = 'LINK',
    ROOT = 'ROOT',
    SYSTEMFOLDER = 'SYSTEM_FOLDER',
    TRANSLATION = 'TRANSLATION'
}

export function PageTypeFromJSON(json: any): PageType {
    return PageTypeFromJSONTyped(json, false);
}

export function PageTypeFromJSONTyped(json: any, ignoreDiscriminator: boolean): PageType {
    return json as PageType;
}

export function PageTypeToJSON(value?: PageType | null): any {
    return value as any;
}
