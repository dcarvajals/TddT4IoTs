"""Strict output contract shared by natural-language CASE workspace generations."""


def _object(properties, required=None):
    return {
        "type": "object",
        "properties": properties,
        "required": required or list(properties.keys()),
        "additionalProperties": False,
    }


POSITION = _object({"x": {"type": "number"}, "y": {"type": "number"}})
ACTOR = _object({
    "id": {"type": "string", "minLength": 1},
    "name": {"type": "string", "minLength": 1},
    "position": POSITION,
})
FLOW_STEP = _object({
    "actor": {"type": "string", "minLength": 1},
    "action": {"type": "string", "minLength": 1},
})
ALTERNATIVE_FLOW = _object({
    "id": {"type": "string", "minLength": 1},
    "startStep": {"type": "integer", "minimum": 1},
    "condition": {"type": "string", "minLength": 1},
    "actor": {"type": "string", "minLength": 1},
    "action": {"type": "string", "minLength": 1},
    "outcome": {"type": "string", "enum": ["return", "end"]},
    "returnStep": {"anyOf": [{"type": "integer", "minimum": 1}, {"type": "null"}]},
})
PARTICIPANT = _object({
    "id": {"type": "string", "minLength": 1},
    "name": {"type": "string", "minLength": 1},
    "type": {"type": "string", "enum": ["actor", "object"]},
})
MESSAGE = _object({
    "from": {"type": "string", "minLength": 1},
    "to": {"type": "string", "minLength": 1},
    "message": {"type": "string", "minLength": 1},
    "type": {"type": "string", "enum": ["sync", "async"]},
    "response": {"type": "string"},
    "responseType": {"anyOf": [{"type": "string", "enum": ["sync", "async"]}, {"type": "null"}]},
})
SEQUENCE = _object({
    "participants": {"type": "array", "items": PARTICIPANT, "minItems": 2},
    "messages": {"type": "array", "items": MESSAGE, "minItems": 1},
})
USE_CASE = _object({
    "id": {"type": "string", "minLength": 1},
    "name": {"type": "string", "minLength": 1},
    "position": POSITION,
    "purpose": {"type": "string", "minLength": 1},
    "precondition": {"type": "string", "minLength": 1},
    "postcondition": {"type": "string", "minLength": 1},
    "description": {"type": "string", "minLength": 1},
    "mainFlow": {"type": "array", "items": FLOW_STEP, "minItems": 1},
    "alternativeFlows": {"type": "array", "items": ALTERNATIVE_FLOW},
    "sequence": SEQUENCE,
})
RELATION = _object({
    "type": {"type": "string", "enum": ["association", "include", "extend", "generalization"]},
    "from": {"type": "string", "minLength": 1},
    "to": {"type": "string", "minLength": 1},
})
DIAGRAM = _object({
    "id": {"type": "string", "minLength": 1},
    "name": {"type": "string", "minLength": 1},
    "actors": {"type": "array", "items": ACTOR},
    "useCases": {"type": "array", "items": USE_CASE, "minItems": 1},
    "relations": {"type": "array", "items": RELATION},
})

CASE_WORKSPACE_SCHEMA = _object({
    "format": {"type": "string", "const": "TDDT4IOTS_CASE_WORKSPACE"},
    "schemaVersion": {"type": "string", "const": "1.0"},
    "project": _object({
        "name": {"type": "string", "minLength": 1},
        "description": {"type": "string"},
        "symbolLanguageRequired": {"type": "boolean"},
    }),
    "diagrams": {"type": "array", "items": DIAGRAM, "minItems": 1},
})


CASE_WORKSPACE_RESPONSE_FORMAT = {
    "type": "json_schema",
    "json_schema": {
        "name": "tddt4iots_case_workspace",
        "strict": True,
        "schema": CASE_WORKSPACE_SCHEMA,
    },
}
