import org.moqui.context.ExecutionContext
import java.time.format.DateTimeFormatter
import java.time.LocalDate

ExecutionContext ec = context.ec

def existingEmailCheck = ec.entity.find("mantle.party.contact.ContactMech")
        .condition("infoString", emailAddress)
        .list()
if (existingEmailCheck) {
    return ec.message.addError("Customer with this email address already exists.")
}

//findservice = ec.service.sync().name("PartyServices.find#Customer").parameters(["infoString": emailAddress]).call();


LocalDate currentDate = LocalDate.now()
DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
String formattedDate = currentDate.format(formatter)

nparty=ec.entity.makeValue("Party")
nparty.setFields(context, true, null, null)
nparty.partyTypeEnumId="Person"
nparty.setSequencedIdPrimary()
nparty.create();

nperson=ec.entity.makeValue("Person")
nperson.setFields(context, true, null, null)
nperson.partyId=nparty.partyId
nperson.create();

ncontactmech=ec.entity.makeValue("ContactMech")
ncontactmech.setFields(context, true, null, null)
ncontactmech.setSequencedIdPrimary()
ncontactmech.contactMechTypeEnumId="CmtEmailAddress"
ncontactmech.infoString=emailAddress
ncontactmech.create();

pcm=ec.entity.makeValue("PartyContactMech")
pcm.setFields(context, true, null, null)
pcm.partyId=nparty.partyId
pcm.contactMechId=ncontactmech.contactMechId
pcm.contactMechPurposeId="EmailPrimary"
pcm.fromDate=formattedDate
pcm.create()
