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
import { AuditReferenceTypeFromJSON, AuditReferenceTypeToJSON, } from './';
export function AuditEntityFromJSON(json) {
    return AuditEntityFromJSONTyped(json, false);
}
export function AuditEntityFromJSONTyped(json, ignoreDiscriminator) {
    if ((json === undefined) || (json === null)) {
        return json;
    }
    return {
        'createdAt': !exists(json, 'createdAt') ? undefined : (new Date(json['createdAt'])),
        'event': !exists(json, 'event') ? undefined : json['event'],
        'id': !exists(json, 'id') ? undefined : json['id'],
        'patch': !exists(json, 'patch') ? undefined : json['patch'],
        'properties': !exists(json, 'properties') ? undefined : json['properties'],
        'referenceId': !exists(json, 'referenceId') ? undefined : json['referenceId'],
        'referenceType': !exists(json, 'referenceType') ? undefined : AuditReferenceTypeFromJSON(json['referenceType']),
        'user': !exists(json, 'user') ? undefined : json['user'],
    };
}
export function AuditEntityToJSON(value) {
    if (value === undefined) {
        return undefined;
    }
    if (value === null) {
        return null;
    }
    return {
        'createdAt': value.createdAt === undefined ? undefined : (value.createdAt.toISOString()),
        'event': value.event,
        'id': value.id,
        'patch': value.patch,
        'properties': value.properties,
        'referenceId': value.referenceId,
        'referenceType': AuditReferenceTypeToJSON(value.referenceType),
        'user': value.user,
    };
}