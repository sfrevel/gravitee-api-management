/* tslint:disable */
/* eslint-disable */
/**
 * Gravitee.io - Management API
 * No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
 *
 * The version of the OpenAPI document: 3.18.0-SNAPSHOT
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

import { exists, mapValues } from '../runtime';
import {
    AlertEventRuleEntity,
    AlertEventRuleEntityFromJSON,
    AlertEventRuleEntityFromJSONTyped,
    AlertEventRuleEntityToJSON,
    AlertReferenceType,
    AlertReferenceTypeFromJSON,
    AlertReferenceTypeFromJSONTyped,
    AlertReferenceTypeToJSON,
    Condition,
    ConditionFromJSON,
    ConditionFromJSONTyped,
    ConditionToJSON,
    Dampening,
    DampeningFromJSON,
    DampeningFromJSONTyped,
    DampeningToJSON,
    Filter,
    FilterFromJSON,
    FilterFromJSONTyped,
    FilterToJSON,
    Notification,
    NotificationFromJSON,
    NotificationFromJSONTyped,
    NotificationToJSON,
    Period,
    PeriodFromJSON,
    PeriodFromJSONTyped,
    PeriodToJSON,
} from './';

/**
 * 
 * @export
 * @interface NewAlertTriggerEntity
 */
export interface NewAlertTriggerEntity {
    /**
     * 
     * @type {string}
     * @memberof NewAlertTriggerEntity
     */
    id?: string;
    /**
     * 
     * @type {string}
     * @memberof NewAlertTriggerEntity
     */
    name: string;
    /**
     * 
     * @type {string}
     * @memberof NewAlertTriggerEntity
     */
    source?: string;
    /**
     * 
     * @type {boolean}
     * @memberof NewAlertTriggerEntity
     */
    enabled?: boolean;
    /**
     * 
     * @type {Array<Condition>}
     * @memberof NewAlertTriggerEntity
     */
    conditions?: Array<Condition>;
    /**
     * 
     * @type {Array<Filter>}
     * @memberof NewAlertTriggerEntity
     */
    filters?: Array<Filter>;
    /**
     * 
     * @type {Dampening}
     * @memberof NewAlertTriggerEntity
     */
    dampening?: Dampening;
    /**
     * 
     * @type {Array<Notification>}
     * @memberof NewAlertTriggerEntity
     */
    notifications?: Array<Notification>;
    /**
     * 
     * @type {{ [key: string]: { [key: string]: string; }; }}
     * @memberof NewAlertTriggerEntity
     */
    metadata?: { [key: string]: { [key: string]: string; }; };
    /**
     * 
     * @type {string}
     * @memberof NewAlertTriggerEntity
     */
    severity?: NewAlertTriggerEntitySeverityEnum;
    /**
     * 
     * @type {string}
     * @memberof NewAlertTriggerEntity
     */
    description?: string;
    /**
     * 
     * @type {Array<Period>}
     * @memberof NewAlertTriggerEntity
     */
    notificationPeriods?: Array<Period>;
    /**
     * 
     * @type {string}
     * @memberof NewAlertTriggerEntity
     */
    type: string;
    /**
     * 
     * @type {boolean}
     * @memberof NewAlertTriggerEntity
     */
    template?: boolean;
    /**
     * 
     * @type {AlertReferenceType}
     * @memberof NewAlertTriggerEntity
     */
    reference_type?: AlertReferenceType;
    /**
     * 
     * @type {string}
     * @memberof NewAlertTriggerEntity
     */
    reference_id?: string;
    /**
     * 
     * @type {Array<AlertEventRuleEntity>}
     * @memberof NewAlertTriggerEntity
     */
    event_rules?: Array<AlertEventRuleEntity>;
}

export function NewAlertTriggerEntityFromJSON(json: any): NewAlertTriggerEntity {
    return NewAlertTriggerEntityFromJSONTyped(json, false);
}

export function NewAlertTriggerEntityFromJSONTyped(json: any, ignoreDiscriminator: boolean): NewAlertTriggerEntity {
    if ((json === undefined) || (json === null)) {
        return json;
    }
    return {
        
        'id': !exists(json, 'id') ? undefined : json['id'],
        'name': json['name'],
        'source': !exists(json, 'source') ? undefined : json['source'],
        'enabled': !exists(json, 'enabled') ? undefined : json['enabled'],
        'conditions': !exists(json, 'conditions') ? undefined : ((json['conditions'] as Array<any>).map(ConditionFromJSON)),
        'filters': !exists(json, 'filters') ? undefined : ((json['filters'] as Array<any>).map(FilterFromJSON)),
        'dampening': !exists(json, 'dampening') ? undefined : DampeningFromJSON(json['dampening']),
        'notifications': !exists(json, 'notifications') ? undefined : ((json['notifications'] as Array<any>).map(NotificationFromJSON)),
        'metadata': !exists(json, 'metadata') ? undefined : json['metadata'],
        'severity': !exists(json, 'severity') ? undefined : json['severity'],
        'description': !exists(json, 'description') ? undefined : json['description'],
        'notificationPeriods': !exists(json, 'notificationPeriods') ? undefined : ((json['notificationPeriods'] as Array<any>).map(PeriodFromJSON)),
        'type': json['type'],
        'template': !exists(json, 'template') ? undefined : json['template'],
        'reference_type': !exists(json, 'reference_type') ? undefined : AlertReferenceTypeFromJSON(json['reference_type']),
        'reference_id': !exists(json, 'reference_id') ? undefined : json['reference_id'],
        'event_rules': !exists(json, 'event_rules') ? undefined : ((json['event_rules'] as Array<any>).map(AlertEventRuleEntityFromJSON)),
    };
}

export function NewAlertTriggerEntityToJSON(value?: NewAlertTriggerEntity | null): any {
    if (value === undefined) {
        return undefined;
    }
    if (value === null) {
        return null;
    }
    return {
        
        'id': value.id,
        'name': value.name,
        'source': value.source,
        'enabled': value.enabled,
        'conditions': value.conditions === undefined ? undefined : ((value.conditions as Array<any>).map(ConditionToJSON)),
        'filters': value.filters === undefined ? undefined : ((value.filters as Array<any>).map(FilterToJSON)),
        'dampening': DampeningToJSON(value.dampening),
        'notifications': value.notifications === undefined ? undefined : ((value.notifications as Array<any>).map(NotificationToJSON)),
        'metadata': value.metadata,
        'severity': value.severity,
        'description': value.description,
        'notificationPeriods': value.notificationPeriods === undefined ? undefined : ((value.notificationPeriods as Array<any>).map(PeriodToJSON)),
        'type': value.type,
        'template': value.template,
        'reference_type': AlertReferenceTypeToJSON(value.reference_type),
        'reference_id': value.reference_id,
        'event_rules': value.event_rules === undefined ? undefined : ((value.event_rules as Array<any>).map(AlertEventRuleEntityToJSON)),
    };
}

/**
* @export
* @enum {string}
*/
export enum NewAlertTriggerEntitySeverityEnum {
    INFO = 'INFO',
    WARNING = 'WARNING',
    CRITICAL = 'CRITICAL'
}

