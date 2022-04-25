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
    Condition,
    ConditionFromJSON,
    ConditionFromJSONTyped,
    ConditionToJSON,
    Projection,
    ProjectionFromJSON,
    ProjectionFromJSONTyped,
    ProjectionToJSON,
    RateConditionAllOf,
    RateConditionAllOfFromJSON,
    RateConditionAllOfFromJSONTyped,
    RateConditionAllOfToJSON,
    SingleValueCondition,
    SingleValueConditionFromJSON,
    SingleValueConditionFromJSONTyped,
    SingleValueConditionToJSON,
} from './';

/**
 * 
 * @export
 * @interface RateCondition
 */
export interface RateCondition extends Condition {
    /**
     * 
     * @type {string}
     * @memberof RateCondition
     */
    operator: RateConditionOperatorEnum;
    /**
     * 
     * @type {number}
     * @memberof RateCondition
     */
    threshold: number;
    /**
     * 
     * @type {SingleValueCondition}
     * @memberof RateCondition
     */
    comparison: SingleValueCondition;
    /**
     * 
     * @type {number}
     * @memberof RateCondition
     */
    duration: number;
    /**
     * 
     * @type {string}
     * @memberof RateCondition
     */
    timeUnit?: RateConditionTimeUnitEnum;
    /**
     * 
     * @type {number}
     * @memberof RateCondition
     */
    sampleSize?: number;
}

export function RateConditionFromJSON(json: any): RateCondition {
    return RateConditionFromJSONTyped(json, false);
}

export function RateConditionFromJSONTyped(json: any, ignoreDiscriminator: boolean): RateCondition {
    if ((json === undefined) || (json === null)) {
        return json;
    }
    return {
        ...ConditionFromJSONTyped(json, ignoreDiscriminator),
        'operator': json['operator'],
        'threshold': json['threshold'],
        'comparison': SingleValueConditionFromJSON(json['comparison']),
        'duration': json['duration'],
        'timeUnit': !exists(json, 'timeUnit') ? undefined : json['timeUnit'],
        'sampleSize': !exists(json, 'sampleSize') ? undefined : json['sampleSize'],
    };
}

export function RateConditionToJSON(value?: RateCondition | null): any {
    if (value === undefined) {
        return undefined;
    }
    if (value === null) {
        return null;
    }
    return {
        ...ConditionToJSON(value),
        'operator': value.operator,
        'threshold': value.threshold,
        'comparison': SingleValueConditionToJSON(value.comparison),
        'duration': value.duration,
        'timeUnit': value.timeUnit,
        'sampleSize': value.sampleSize,
    };
}

/**
* @export
* @enum {string}
*/
export enum RateConditionOperatorEnum {
    LT = 'LT',
    LTE = 'LTE',
    GTE = 'GTE',
    GT = 'GT'
}
/**
* @export
* @enum {string}
*/
export enum RateConditionTimeUnitEnum {
    NANOSECONDS = 'NANOSECONDS',
    MICROSECONDS = 'MICROSECONDS',
    MILLISECONDS = 'MILLISECONDS',
    SECONDS = 'SECONDS',
    MINUTES = 'MINUTES',
    HOURS = 'HOURS',
    DAYS = 'DAYS'
}

