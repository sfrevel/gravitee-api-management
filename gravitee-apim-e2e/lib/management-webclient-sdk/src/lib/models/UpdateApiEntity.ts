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
    ApiLifecycleState,
    ApiLifecycleStateFromJSON,
    ApiLifecycleStateFromJSONTyped,
    ApiLifecycleStateToJSON,
    ApiMetadataEntity,
    ApiMetadataEntityFromJSON,
    ApiMetadataEntityFromJSONTyped,
    ApiMetadataEntityToJSON,
    Flow,
    FlowFromJSON,
    FlowFromJSONTyped,
    FlowToJSON,
    Plan,
    PlanFromJSON,
    PlanFromJSONTyped,
    PlanToJSON,
    PropertyEntity,
    PropertyEntityFromJSON,
    PropertyEntityFromJSONTyped,
    PropertyEntityToJSON,
    Proxy,
    ProxyFromJSON,
    ProxyFromJSONTyped,
    ProxyToJSON,
    Resource,
    ResourceFromJSON,
    ResourceFromJSONTyped,
    ResourceToJSON,
    ResponseTemplate,
    ResponseTemplateFromJSON,
    ResponseTemplateFromJSONTyped,
    ResponseTemplateToJSON,
    Rule,
    RuleFromJSON,
    RuleFromJSONTyped,
    RuleToJSON,
    Services,
    ServicesFromJSON,
    ServicesFromJSONTyped,
    ServicesToJSON,
    Visibility,
    VisibilityFromJSON,
    VisibilityFromJSONTyped,
    VisibilityToJSON,
} from './';

/**
 *
 * @export
 * @interface UpdateApiEntity
 */
export interface UpdateApiEntity {
    /**
     * API's crossId. Identifies API across environments.
     * @type {string}
     * @memberof UpdateApiEntity
     */
    crossId?: string;
    /**
     * Api's name. Duplicate names can exists.
     * @type {string}
     * @memberof UpdateApiEntity
     */
    name: string;
    /**
     * Api's version. It's a simple string only used in the portal.
     * @type {string}
     * @memberof UpdateApiEntity
     */
    version: string;
    /**
     * API's description. A short description of your API.
     * @type {string}
     * @memberof UpdateApiEntity
     */
    description: string;
    /**
     *
     * @type {Services}
     * @memberof UpdateApiEntity
     */
    services?: Services;
    /**
     * The list of API resources used by policies like cache resources or oauth2
     * @type {Array<Resource>}
     * @memberof UpdateApiEntity
     */
    resources?: Array<Resource>;
    /**
     *
     * @type {Visibility}
     * @memberof UpdateApiEntity
     */
    visibility: Visibility;
    /**
     * the list of sharding tags associated with this API.
     * @type {Array<string>}
     * @memberof UpdateApiEntity
     */
    tags?: Array<string>;
    /**
     * the API logo encoded in base64
     * @type {string}
     * @memberof UpdateApiEntity
     */
    picture?: string;
    /**
     * the list of categories associated with this API
     * @type {Array<string>}
     * @memberof UpdateApiEntity
     */
    categories?: Array<string>;
    /**
     * the free list of labels associated with this API
     * @type {Array<string>}
     * @memberof UpdateApiEntity
     */
    labels?: Array<string>;
    /**
     * API's groups. Used to add team in your API.
     * @type {Array<string>}
     * @memberof UpdateApiEntity
     */
    groups?: Array<string>;
    /**
     *
     * @type {Array<ApiMetadataEntity>}
     * @memberof UpdateApiEntity
     */
    metadata?: Array<ApiMetadataEntity>;
    /**
     * the API background encoded in base64
     * @type {string}
     * @memberof UpdateApiEntity
     */
    background?: string;
    /**
     *
     * @type {Proxy}
     * @memberof UpdateApiEntity
     */
    proxy: Proxy;
    /**
     * a map where you can associate a path to a configuration (the policies configuration)
     * @type {{ [key: string]: Array<Rule>; }}
     * @memberof UpdateApiEntity
     */
    paths?: { [key: string]: Array<Rule>; };
    /**
     * a list of flows (the policies configuration)
     * @type {Array<Flow>}
     * @memberof UpdateApiEntity
     */
    flows?: Array<Flow>;
    /**
     * a list of plans with flows (the policies configuration)
     * @type {Array<Plan>}
     * @memberof UpdateApiEntity
     */
    plans?: Array<Plan>;
    /**
     * A dictionary (could be dynamic) of properties available in the API context.
     * @type {Array<PropertyEntity>}
     * @memberof UpdateApiEntity
     */
    properties?: Array<PropertyEntity>;
    /**
     * API's gravitee definition version
     * @type {string}
     * @memberof UpdateApiEntity
     */
    gravitee?: string;
    /**
     * API's flow mode.
     * @type {string}
     * @memberof UpdateApiEntity
     */
    flow_mode?: UpdateApiEntityFlowModeEnum;
    /**
     * the API logo URL
     * @type {string}
     * @memberof UpdateApiEntity
     */
    picture_url?: string;
    /**
     * A list of paths used to aggregate data in analytics
     * @type {Array<string>}
     * @memberof UpdateApiEntity
     */
    path_mappings?: Array<string>;
    /**
     * A map that allows you to configure the output of a request based on the event throws by the gateway. Example : Quota exceeded, api-ky is missing, ...
     * @type {{ [key: string]: { [key: string]: ResponseTemplate; }; }}
     * @memberof UpdateApiEntity
     */
    response_templates?: { [key: string]: { [key: string]: ResponseTemplate; }; };
    /**
     *
     * @type {ApiLifecycleState}
     * @memberof UpdateApiEntity
     */
    lifecycle_state?: ApiLifecycleState;
    /**
     *
     * @type {boolean}
     * @memberof UpdateApiEntity
     */
    disable_membership_notifications?: boolean;
    /**
     * the API background URL
     * @type {string}
     * @memberof UpdateApiEntity
     */
    background_url?: string;
}

export function UpdateApiEntityFromJSON(json: any): UpdateApiEntity {
    return UpdateApiEntityFromJSONTyped(json, false);
}

export function UpdateApiEntityFromJSONTyped(json: any, ignoreDiscriminator: boolean): UpdateApiEntity {
    if ((json === undefined) || (json === null)) {
        return json;
    }
    return {

        'crossId': !exists(json, 'crossId') ? undefined : json['crossId'],
        'name': json['name'],
        'version': json['version'],
        'description': json['description'],
        'services': !exists(json, 'services') ? undefined : ServicesFromJSON(json['services']),
        'resources': !exists(json, 'resources') ? undefined : ((json['resources'] as Array<any>).map(ResourceFromJSON)),
        'visibility': VisibilityFromJSON(json['visibility']),
        'tags': !exists(json, 'tags') ? undefined : json['tags'],
        'picture': !exists(json, 'picture') ? undefined : json['picture'],
        'categories': !exists(json, 'categories') ? undefined : json['categories'],
        'labels': !exists(json, 'labels') ? undefined : json['labels'],
        'groups': !exists(json, 'groups') ? undefined : json['groups'],
        'metadata': !exists(json, 'metadata') ? undefined : ((json['metadata'] as Array<any>).map(ApiMetadataEntityFromJSON)),
        'background': !exists(json, 'background') ? undefined : json['background'],
        'proxy': ProxyFromJSON(json['proxy']),
        'paths': !exists(json, 'paths') ? undefined : json['paths'],
        'flows': !exists(json, 'flows') ? undefined : ((json['flows'] as Array<any>).map(FlowFromJSON)),
        'plans': !exists(json, 'plans') ? undefined : ((json['plans'] as Array<any>).map(PlanFromJSON)),
        'properties': !exists(json, 'properties') ? undefined : ((json['properties'] as Array<any>).map(PropertyEntityFromJSON)),
        'gravitee': !exists(json, 'gravitee') ? undefined : json['gravitee'],
        'flow_mode': !exists(json, 'flow_mode') ? undefined : json['flow_mode'],
        'picture_url': !exists(json, 'picture_url') ? undefined : json['picture_url'],
        'path_mappings': !exists(json, 'path_mappings') ? undefined : json['path_mappings'],
        'response_templates': !exists(json, 'response_templates') ? undefined : json['response_templates'],
        'lifecycle_state': !exists(json, 'lifecycle_state') ? undefined : ApiLifecycleStateFromJSON(json['lifecycle_state']),
        'disable_membership_notifications': !exists(json, 'disable_membership_notifications') ? undefined : json['disable_membership_notifications'],
        'background_url': !exists(json, 'background_url') ? undefined : json['background_url'],
    };
}

export function UpdateApiEntityToJSON(value?: UpdateApiEntity | null): any {
    if (value === undefined) {
        return undefined;
    }
    if (value === null) {
        return null;
    }
    return {

        'crossId': value.crossId,
        'name': value.name,
        'version': value.version,
        'description': value.description,
        'services': ServicesToJSON(value.services),
        'resources': value.resources === undefined ? undefined : ((value.resources as Array<any>).map(ResourceToJSON)),
        'visibility': VisibilityToJSON(value.visibility),
        'tags': value.tags,
        'picture': value.picture,
        'categories': value.categories,
        'labels': value.labels,
        'groups': value.groups,
        'metadata': value.metadata === undefined ? undefined : ((value.metadata as Array<any>).map(ApiMetadataEntityToJSON)),
        'background': value.background,
        'proxy': ProxyToJSON(value.proxy),
        'paths': value.paths,
        'flows': value.flows === undefined ? undefined : ((value.flows as Array<any>).map(FlowToJSON)),
        'plans': value.plans === undefined ? undefined : ((value.plans as Array<any>).map(PlanToJSON)),
        'properties': value.properties === undefined ? undefined : ((value.properties as Array<any>).map(PropertyEntityToJSON)),
        'gravitee': value.gravitee,
        'flow_mode': value.flow_mode,
        'picture_url': value.picture_url,
        'path_mappings': value.path_mappings,
        'response_templates': value.response_templates,
        'lifecycle_state': ApiLifecycleStateToJSON(value.lifecycle_state),
        'disable_membership_notifications': value.disable_membership_notifications,
        'background_url': value.background_url,
    };
}

/**
* @export
* @enum {string}
*/
export enum UpdateApiEntityFlowModeEnum {
    DEFAULT = 'DEFAULT',
    BESTMATCH = 'BEST_MATCH'
}

