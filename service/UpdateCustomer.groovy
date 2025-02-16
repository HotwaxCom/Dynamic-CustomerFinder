import org.moqui.context.ExecutionContext
import org.moqui.entity.EntityValue
import java.sql.Timestamp

ExecutionContext ec = context.ec
Timestamp today = ec.user.nowTimestamp

EntityValue partyContactMech = ec.entity.find("mantle.party.contact.PartyContactMech")
        .condition("contactMechPurposeId", "EmailPrimary")
        .condition("contactMechId", ec.entity.find("mantle.party.contact.ContactMech")
                .condition("infoString", emailAddress)
                //.condition("contactMechTypeEnumId", "CmtEmailAddress")
                .useCache(false)
                .one()?.contactMechId)
        .useCache(false)
        .one()

if (!partyContactMech) {
    ec.message.addError("No customer found with email ${emailAddress}.")
    return
}

String partyId = partyContactMech.partyId

EntityValue person = ec.entity.find("mantle.party.Person")
        .condition("partyId", partyId)
        .one()

if (person) {
    if (firstName) person.firstName = firstName
    if (lastName) person.lastName = lastName
    person.update()
}

if (contactNumber) {
    EntityValue oldTelecomMech = ec.entity.find("mantle.party.contact.PartyContactMech")
            .condition("partyId", partyId)
            .condition("contactMechPurposeId", "PhonePrimary")
            .one()

    if (oldTelecomMech) {
        oldTelecomMech.thruDate = today
        oldTelecomMech.update()
    }

    EntityValue telecomContactMech = ec.entity.makeValue("mantle.party.contact.ContactMech")
    telecomContactMech.setSequencedIdPrimary()
    telecomContactMech.contactMechTypeEnumId = "CmtTelecomNumber"
    telecomContactMech.create()
    String telecomContactMechId = telecomContactMech.contactMechId

    EntityValue telecomNumber = ec.entity.makeValue("mantle.party.contact.TelecomNumber")
    telecomNumber.contactMechId = telecomContactMechId
    telecomNumber.contactNumber = contactNumber
    telecomNumber.create()


    EntityValue newPartyContactMech = ec.entity.makeValue("mantle.party.contact.PartyContactMech")
    newPartyContactMech.partyId = partyId
    newPartyContactMech.contactMechId = telecomContactMechId
    newPartyContactMech.contactMechPurposeId = "PhonePrimary"
    newPartyContactMech.fromDate = today
    newPartyContactMech.create()

    context.telecomContactMechId = telecomContactMechId
}

if (address1) {
    EntityValue oldPostalMech = ec.entity.find("mantle.party.contact.PartyContactMech")
            .condition("partyId", partyId)
            .condition("contactMechPurposeId", "PostalPrimary")
            .one()

    if (oldPostalMech) {
        oldPostalMech.thruDate = today
        oldPostalMech.update()
    }

    EntityValue postalContactMech = ec.entity.makeValue("mantle.party.contact.ContactMech")
    postalContactMech.setSequencedIdPrimary()
    postalContactMech.contactMechTypeEnumId = "CmtPostalAddress"
    postalContactMech.create()
    String postalContactMechId = postalContactMech.contactMechId

    EntityValue postalAddress = ec.entity.makeValue("contact.PostalAddress")
    postalAddress.contactMechId = postalContactMechId
    postalAddress.address1 = address1
    postalAddress.city = city
    postalAddress.postalCode = postalCode
    postalAddress.create()

    EntityValue newPartyContactMech = ec.entity.makeValue("mantle.party.contact.PartyContactMech")
    newPartyContactMech.partyId = partyId
    newPartyContactMech.contactMechId = postalContactMechId
    newPartyContactMech.contactMechPurposeId = "PostalPrimary"
    newPartyContactMech.fromDate = today
    newPartyContactMech.create()

    context.postalContactMechId = postalContactMechId
}

context.partyId = partyId
