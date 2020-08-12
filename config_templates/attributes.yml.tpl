########################################################################################
# CONFIGURATION FILE FOR ATTRIBUTES
# ENTRIES SHOULD BE GIVEN IN THE FOLLOWING FORMAT
# - identifier: rpc_name
#   rpcName: rpc_name
#   ldapName: ldap_name
#   attrType: type
#   separator: ,
# WHERE attrType HAS TO BE ONE OF THE FOLLOWING:
#   STRING, LARGE_STRING, INTEGER, BOOLEAN, ARRAY, LARGE_ARRAY, MAP_JSON, MAP_KEY_VALUE
# WHERE separator is used only if the attrType is MAP_KEY_VALUE
########################################################################################
- identifier: urn:perun:example:name
  rpcName: urn:perun:example:name
  ldapName: exampleLdapName
  attrType: STRING
- identifier: urn:perun:example:name2
  rpcName: urn:perun:example:name2
  ldapName: exampleLdapName2
  attrType: MAP_KEY_VALUE
  separator: "-"
