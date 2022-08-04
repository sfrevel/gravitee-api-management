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
export function HttpClientOptionsFromJSON(json) {
    return HttpClientOptionsFromJSONTyped(json, false);
}
export function HttpClientOptionsFromJSONTyped(json, ignoreDiscriminator) {
    if ((json === undefined) || (json === null)) {
        return json;
    }
    return {
        'clearTextUpgrade': !exists(json, 'clearTextUpgrade') ? undefined : json['clearTextUpgrade'],
        'connectTimeout': !exists(json, 'connectTimeout') ? undefined : json['connectTimeout'],
        'followRedirects': !exists(json, 'followRedirects') ? undefined : json['followRedirects'],
        'idleTimeout': !exists(json, 'idleTimeout') ? undefined : json['idleTimeout'],
        'keepAlive': !exists(json, 'keepAlive') ? undefined : json['keepAlive'],
        'maxConcurrentConnections': !exists(json, 'maxConcurrentConnections') ? undefined : json['maxConcurrentConnections'],
        'pipelining': !exists(json, 'pipelining') ? undefined : json['pipelining'],
        'readTimeout': !exists(json, 'readTimeout') ? undefined : json['readTimeout'],
        'useCompression': !exists(json, 'useCompression') ? undefined : json['useCompression'],
        'version': !exists(json, 'version') ? undefined : json['version'],
    };
}
export function HttpClientOptionsToJSON(value) {
    if (value === undefined) {
        return undefined;
    }
    if (value === null) {
        return null;
    }
    return {
        'clearTextUpgrade': value.clearTextUpgrade,
        'connectTimeout': value.connectTimeout,
        'followRedirects': value.followRedirects,
        'idleTimeout': value.idleTimeout,
        'keepAlive': value.keepAlive,
        'maxConcurrentConnections': value.maxConcurrentConnections,
        'pipelining': value.pipelining,
        'readTimeout': value.readTimeout,
        'useCompression': value.useCompression,
        'version': value.version,
    };
}
/**
* @export
* @enum {string}
*/
export var HttpClientOptionsVersionEnum;
(function (HttpClientOptionsVersionEnum) {
    HttpClientOptionsVersionEnum["_11"] = "HTTP_1_1";
    HttpClientOptionsVersionEnum["_2"] = "HTTP_2";
})(HttpClientOptionsVersionEnum || (HttpClientOptionsVersionEnum = {}));