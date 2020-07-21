#############################################################
# CONFIGURATION FILE FOR FACADE LAYER, PART RELYING PARTY
# YOU CAN EXTEND THE CONFIGURATION PART WHATEVER YOU LIKE
# ENTRIES SHOULD BE GIVEN IN THE FOLLOWING FORMAT
# - methodName: "some_method_name" (use underscore notation)
#   configuration:
#     adapter: "RPC" (either RPC or LDAP)
#     optionX: 123
#############################################################
- methodName: "find_by_identifiers"
  configuration:
    adapter: "RPC"
