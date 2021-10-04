// Infrastructural variables
variable "product" {
}

variable "component" {
}

variable "env" {
}

variable "location" {
  default = "UK South"
}

variable "common_tags" {
  type = map(string)
}

variable "tenant_id" {
  description = "(Required) The Azure Active Directory tenant ID that should be used for authenticating requests to the key vault. This is usually sourced from environment variables and not normally required to be specified."
}

variable "jenkins_AAD_objectId" {
  description = "(Required) The Azure AD object ID of a user, service principal or security group in the Azure Active Directory tenant for the vault. The object ID must be unique for the list of access policies."
}

variable "subscription" {
}

////////////////////////////////
// Database
////////////////////////////////

// Define the default values for optional parameters (see https://github.com/hmcts/cnp-module-postgres)
variable "sku_name" {
  default = "GP_Gen5_2"
}

variable "sku_tier" {
  default = "GeneralPurpose"
}

variable "storage_mb" {
  default = "51200"
}

variable "sku_capacity" {
  default = "2"
}

variable "ssl_enforcement" {
  default = "Enabled"
}

variable "backup_retention_days" {
  default = "35"
}

variable "georedundant_backup" {
  default = "Enabled"
}

// Define the values for mandatory/required parameters (see https://github.com/hmcts/cnp-module-postgres)

variable "postgresql_user" {
  default = "hmc"
}

variable "database_name" {
  default = "hmc_hearing_management_component"
}

variable "postgresql_version" {
  default = "11"
}
