import kotlinx.datetime.*
import java.io.File
import kotlinx.serialization.*
import kotlinx.serialization.json.*



@Serializable
sealed class Contact {
    var firstName: String = "[no data]"
        set(value) {
            val letter = value.first().toString()
            field = letter.uppercase() + value.removePrefix(letter)
        }
    var telNumber: String = "[no number]"
        set(value) {
            field = if (checkPhoneNumber(value) || value == "+(phone)" || value == "+(another)") value
            else "[no number]"
        }
    var timeCreated = "[no data]"
    var timeLastEdit = "[no data]"


    fun getDateAndTime(): String {
        val currentInstant = Clock.System.now()
        val tz = TimeZone.currentSystemDefault()
        return currentInstant.toLocalDateTime(tz).toString().substringBefore('.')
    }
    fun addTelNumber() {
        print("Enter the number: ")
        telNumber = readln()
    }
    private fun checkPhoneNumber(number: String): Boolean {
        val regex = "[+]?[-|\\s]?(\\d{1,3})?[-|\\s]?([(\\dA-Za-z)]{3,5})?(([-|\\s]?[\\dA-Za-z]{2,4})+)?".toRegex()//"[+]?\\d?[-|\\s]?[(\\dA-Za-z)]{3,5}([-|\\s]?[\\dA-Za-z]{2,4})+".toRegex()
        return regex.matches(number)
    }
//    fun hasNumber(contact: Contact): Boolean {
//        return contact.telNumber != "[no number]"
//    }



    open fun addContact() {
        timeCreated = getDateAndTime()
        timeLastEdit = getDateAndTime()
    }
    open fun editContact() {}
    open fun printContact() {}



}
class Action : Contact() {
    fun countContacts(listContacts: MutableList<Contact>) {
        if (listContacts.isEmpty()) println("The Phone Book has 0 records.")
        else println("The Phone Book has ${listContacts.size} records.")
    }
    fun listContacts(listContacts: List<Contact>) {
        if (listContacts.isEmpty()) println("The Phone Book has 0 records.")
        else listContacts.forEach {
            print("${listContacts.indexOf(it) + 1}. ")
            println(it.toString())
        }
    }
    fun removeContact(listContacts: MutableList<Contact>, index: Int) {
        if (listContacts.isNotEmpty()){
            println("Contact deleted")
            listContacts.removeAt(index)
        } else {
            println("No records to remove!")
            return
        }
    }
    fun search(listContacts: MutableList<Contact>): List<Contact> {
        val listQuery = mutableListOf<Contact>()
        print("Enter search query: ")
        val regex = ".*?${readln().lowercase()}.*?".toRegex()
        listContacts.forEach {
            if (it is ContactHuman) {
                if (it.firstName.lowercase().matches(regex) || it.secondName.lowercase().matches(regex))listQuery.add(it)
            }
            if (it is ContactCompany) {
                if (it.firstName.lowercase().matches(regex) || it.addressCompany.lowercase().matches(regex))listQuery.add(it)
            }
        }
        return listQuery.toList()
    }
}
@Serializable
class ContactHuman: Contact() {
    var secondName: String = "[no data]"
        set(value) {
            val letter = value.first().toString()
            field = letter.uppercase() + value.removePrefix(letter)
        }
    var genderOfPerson = "[no data]"
    var dateOfBirth = "[no data]"

    override fun toString(): String {
        return "$firstName $secondName"
    }
    override fun addContact() {
        super.addContact()
        print("Enter the name of the person: ")
        firstName = readln()
        print("Enter the surname of the person: ")
        secondName = readln()
        print("Enter the gender (M, F): ")
        val gender = readln()
        genderOfPerson = gender.ifEmpty {
            println("Bad gender!")
            "[no data]"
        }
        print("Enter the birth date: ")
        val birth = readln()
        dateOfBirth = birth.ifEmpty {
            println("Bad birth date!")
            "[no data]"
        }
        print("Enter the number: ")
        telNumber = readln()
    }
    override fun printContact() {
        println("Name: $firstName")
        println("Surname: $secondName")
        println("Birth date: $dateOfBirth")
        println("Gender: $genderOfPerson")
        println("Number: $telNumber")
        println("Time created: $timeCreated")
        println("Time last edit: $timeLastEdit")
    }
    override fun editContact() {
        print("Select a field (name, surname, birth, gender, number): ")
        when(readln()) {
            "name" -> this.addName()
            "surname" -> this.addSurname()
            "birth" -> this.addBirth()
            "gender" -> this.addGender()
            "number" -> this.addTelNumber()
        }
        this.timeLastEdit = getDateAndTime()
        println("The record updated!")

    }




    fun addName() {
        print("Enter the name of the person: ")
        firstName = readln()
    }
    fun addSurname() {
        print("Enter the surname of the person: ")
        secondName = readln()
    }
    fun addGender() {
        print("Enter the gender (M, F): ")
        val input = readln()
        genderOfPerson = input.ifEmpty {
            println("Bad gender!")
            "[no data]"
        }
    }
    fun addBirth() {
        print("Enter the birth date: ")
        val input = readln()
        dateOfBirth = input.ifEmpty {
            println("Bad birth date!")
            "[no data]"
        }
    }


}
@Serializable
class ContactCompany: Contact() {
    var addressCompany = "no data"

    override fun toString(): String {
        return firstName
    }
    override fun addContact() {
        super.addContact()
        print("Enter the organization name: ")
        firstName = readln()
        print("Enter the address: ")
        addressCompany = readln()
        print("Enter the number: ")
        telNumber = readln()
    }
    override fun printContact() {
        println("Organization name: $firstName")
        println("Address: $addressCompany")
        println("Number: $telNumber")
        println("Time created: $timeCreated")
        println("Time last edit: $timeLastEdit")
    }
    override fun editContact() {
        print("Select a field (name, address, number): ")
        when(readln()) {
            "name" -> this.addName()
            "address" -> this.addAddress()
            "number" -> this.addTelNumber()
        }
        this.timeLastEdit = getDateAndTime()
        println("The record updated!")
    }


    fun addName() {
        print("Enter the organization name: ")
        firstName = readln()
    }
    fun addAddress() {
        print("Enter the address: ")
        addressCompany = readln()
    }

}

fun main(args: Array<String>) {
    val searchList = emptyList<Contact>().toMutableList()
    var transitions = ""
    var tempIndex = 0
    var notebookContact = mutableListOf<Contact>()
    val fileName = if (args.isNotEmpty()
        && args.size == 2
        && args[0] == "open"
        && args[1].isNotEmpty()) {
        args[1].substringBefore('.')
    } else "Contacts"
    val action = Action()
    val tempFile = FileAction(fileName)
    if (tempFile.checkingFile()) notebookContact = jsonToListObject(tempFile.loadFile()).toMutableList()

    exitPoint@while (true) {
        while (transitions == "editMenu") {
            println()
            print("[record] Enter action (edit, delete, menu): ")
            when(readln()) {
                "edit" -> {
                    notebookContact[tempIndex].editContact()
                    val output = listObjectToJson(notebookContact)
                    tempFile.saveFile(output)
                    print("Saved")
                    notebookContact[tempIndex].printContact()
                }
                "delete" -> {
                    action.removeContact(notebookContact, tempIndex)
                    val output = listObjectToJson(notebookContact)
                    tempFile.saveFile(output)
                    print("Saved")
                }
                "menu" -> {
                    break
                }
            }
        }
        while (true) {
            val person = ContactHuman()
            val company = ContactCompany()
            println()
            print("[menu] Enter action (add, list, search, count, exit): ")
            when(readln()) {
                "add" -> {
                    print("Enter the type (person, organization): ")
                    when(readln()) {
                        "person" -> {
                            notebookContact.add(person)
                            notebookContact[notebookContact.lastIndex].addContact()
                        }
                        "organization" -> {
                            notebookContact.add(company)
                            notebookContact[notebookContact.lastIndex].addContact()
                        }
                    }
                    val output = listObjectToJson(notebookContact)
                    tempFile.saveFile(output)
                }
                "list" -> {
                    action.listContacts(notebookContact)
                    transitions = "list"
                    break

                }
                "search" -> {
                    searchList.clear()
                    searchList.addAll(action.search(notebookContact))
                    if (searchList.isEmpty() || searchList.size == 1) println("Found ${searchList.size} result:")
                    else println("Found ${searchList.size} results:")
                    action.listContacts(searchList)
                    transitions = "searchMenu"
                    break
                }
                "count" -> action.countContacts(notebookContact)
                "exit" -> {
                    tempFile.deleteFile()
                    break@exitPoint
                }
            }
        }
        while (transitions == "list") {
            println()
            print("[list] Enter action ([number], back): ")
            val objectNum = readln()
            val regex = "\\d+".toRegex()
            if (!objectNum.matches(regex) && objectNum == "back") {
                break
            }
            if (objectNum.matches(regex)) {
                tempIndex = objectNum.toInt() - 1
                notebookContact[tempIndex].printContact()
                transitions = "editMenu"
                break
            }

        }
        while (transitions == "searchMenu") {
            println()
            print("[search] Enter action ([number], back, again): ")
            val objectNum = readln()
            val regex = "\\d+".toRegex()
            if (!objectNum.matches(regex) && objectNum == "back") {
                break
            }
            if (!objectNum.matches(regex) && objectNum == "again") {
                searchList.clear()
                searchList.addAll(action.search(notebookContact))
                if (searchList.isEmpty() || searchList.size == 1) println("Found ${searchList.size} result:")
                else println("Found ${searchList.size} results:")
                action.listContacts(searchList)
            }
            if (objectNum.matches(regex)) {
                val searchIndex = objectNum.toInt() - 1
                searchList[searchIndex].printContact()
                tempIndex = notebookContact.indexOf(searchList[searchIndex])
                transitions = "editMenu"
                break
            }
        }
    }
}


class FileAction(fileName: String) {
    private val separator = File.separator
    private val _fileName = ".${this.separator}$fileName"
//            "C:${this.separator}aplikation" +
//            "${this.separator}spring" +
//            "${this.separator}testNew" +
//            "${this.separator}src${this.separator}$fileName"

    fun loadFile(): String {
        return File(_fileName).readText()
    }

    fun saveFile(data: String) {
        File(_fileName).writeText(data)
    }
    fun deleteFile() {
        File(_fileName).delete()
    }
    fun checkingFile(): Boolean {
        return File(_fileName).exists()
    }
}
fun listObjectToJson(list: List<Contact>) = Json.encodeToString(list)
fun jsonToListObject(str: String) = Json.decodeFromString<List<Contact>>(str)