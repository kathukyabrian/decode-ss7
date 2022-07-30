package tech.kitucode.ss7;

public class App {

    public static void main(String[] args) {

        byte[] bytes = {
                (byte) 0xa1,
                (byte) 0x1d,
                (byte) 0x02,
                (byte) 0x01,
                (byte) 0x01,
                (byte) 0x02,
                (byte) 0x01,
                (byte) 0x2d,
                (byte) 0x30,
                (byte) 0x15,
                (byte) 0x80,
                (byte) 0x07,
                (byte) 0x91,
                (byte) 0x52,
                (byte) 0x74,
                (byte) 0x22,
                (byte) 0x48,
                (byte) 0x08,
                (byte) 0x26,
                (byte) 0x81,
                (byte) 0x01,
                (byte) 0x01,
                (byte) 0x82,
                (byte) 0x07,
                (byte) 0x91,
                (byte) 0x52,
                (byte) 0x74,
                (byte) 0x22,
                (byte) 0x05,
                (byte) 0x00,
                (byte) 0x11
        };
        int position = 0;
        int lastMarkedPosition = 0;
        // the first byte represents a tag -> the root tag
        position += 1;

        // the 2nd byte represents the length of the whole message
        int totalSize = bytes[position] & 0xff;
        System.out.println("Total size of the MAP message is " + totalSize + " bytes\n");
        position += 1;

        // the third byte represents yet another tag
        position += 1;
        // the  fourth byte represents the length of the tag
        int sizeOfInvokeIdTag = bytes[position] & 0xff;
        lastMarkedPosition = position;
        position += sizeOfInvokeIdTag;

        // the fifth byte represents the value of invokeId tag
        if (position - lastMarkedPosition > 1) {
        } else {
            int invokeIdValue = bytes[position] & 0xff;
            System.out.println("Invoke id is " + invokeIdValue + "\n");
        }
        position += 1;

        // the sixth byte represents a tag(localvalue)
        position += 1;

        // the seventh byte represents the length of localvalue tag
        int sizeOfOperationCode = bytes[position] & 0xff;
        lastMarkedPosition = position;
        position = position + sizeOfOperationCode;

        // the eighth byte is the value of localvalue tag
        if (position - lastMarkedPosition > 1) {
        } else {
            int operationCode = bytes[position] & 0xff;
            System.out.println("OperationCode is " + operationCode + "(" + resolveMAPOperation(operationCode) + ")\n");
        }
        position += 1;

        // the 9th byte is a tag
        position += 1;

        // the 10th byte is length of a tag
        position += 1;

        // the 11th byte is a tag
        position += 1;

        // the 12th byte is length of a tag
        int lengthOfMSISDN = bytes[position] & 0xff;
        lastMarkedPosition = position;
        position += lengthOfMSISDN;

        // here sth crazy happens
        // the next n bytes represent the MSISDN
        // after this we shall land into a tag
        if (position - lastMarkedPosition > 1) {

            // create a value to store the number
            long MSISDNValue = 0l;
            for (int i = lastMarkedPosition + 1; i <= position; i++) {
                int shiftBy = (position - i) * 8;
                long value = bytes[i] & 0xff;
                MSISDNValue |= value << shiftBy;
            }
            String phoneNumber = decodePhoneNumber(MSISDNValue);
            System.out.println("\t\tMSISDN : " + phoneNumber + "\n");

        } else {
            System.out.println("Straight forward case here");
        }
        position += 1;

        // the next byte represents a tag
        position += 1;

        // tag length for above tag
        int tagLengthForSMRPPRI = bytes[position] & 0xff;
        lastMarkedPosition = position;
        position += tagLengthForSMRPPRI;

        // value for tag above
        if (position - lastMarkedPosition > 1) {
        } else {
            int smrrpri = bytes[position] & 0xff;
            System.out.println("SM-RR-PRI is " + smrrpri + "\n");
        }
        position += 1;

        // this represents a tag
        position += 1;

        // this represents length of previous tag
        int lengthOfServiceCenterAddress = bytes[position] & 0xff;
        lastMarkedPosition = position;
        position += lengthOfServiceCenterAddress;

        // this represents the value of previous tag
        long serviceCenterAddress = 0l;
        if (position - lastMarkedPosition > 1) {

            // create a value to store the number
            for (int i = lastMarkedPosition + 1; i <= position; i++) {
                int shiftBy = (position - i) * 8;
                long value = bytes[i] & 0xff;
                serviceCenterAddress |= value << shiftBy;
            }

        } else {
            System.out.println("Straight forward case here");
        }
        String phoneNumber = decodePhoneNumber(serviceCenterAddress);
        System.out.println("\t\tService Center Address : " + phoneNumber + "\n");
        position += 1;
    }

    public static String decodePhoneNumber(long encodedAddress) {
        System.out.println("MSISDN decoding based on ITU-T E.164");

        String phoneNumber = "";

        //correct this
        long extensionMask = 0b10000000000000000000000000000000000000000000000000000000l;
        long extension = (encodedAddress & extensionMask);
        if (extension > 0) {
            System.out.println("\t\tExtension : No extension");
        } else {
            System.out.println("\t\tExtension : There is extension");
        }

        long natureOfNumberMask = 0b01110000000000000000000000000000000000000000000000000000l;
        long natureOfNumber = encodedAddress & natureOfNumberMask;
        if (natureOfNumber > 0) {
            System.out.println("\t\tNature of number : International Number");
        } else {
            System.out.println("\t\tNature of number : Local Number");
        }

        long firstNumberMask = 0b00000000111100000000000000000000000000000000000000000000l;
        long secondNumberMask = 0b00000000000011110000000000000000000000000000000000000000l;
        long thirdNumberMask = 0b00000000000000001111000000000000000000000000000000000000l;
        long fourthNumberMask = 0b00000000000000000000111100000000000000000000000000000000l;
        long fifthNumberMask = 0b00000000000000000000000011110000000000000000000000000000l;
        long sixthNumberMask = 0b00000000000000000000000000001111000000000000000000000000l;
        long seventhNumberMask = 0b00000000000000000000000000000000111100000000000000000000l;
        long eighthNumberMask = 0b00000000000000000000000000000000000011110000000000000000l;
        long ninthNumberMask = 0b00000000000000000000000000000000000000001111000000000000l;
        long tenthNumberMask = 0b00000000000000000000000000000000000000000000111100000000l;
        long eleventhNumberMask = 0b00000000000000000000000000000000000000000000000011110000l;
        long twelfthNumberMask = 0b00000000000000000000000000000000000000000000000000001111l;

        long firstNumber = (encodedAddress & firstNumberMask) >> 44;
        long secondNumber = (encodedAddress & secondNumberMask) >> 40;
        long thirdNumber = (encodedAddress & thirdNumberMask) >> 36;
        long fourthNumber = (encodedAddress & fourthNumberMask) >> 32;
        long fifthNumber = (encodedAddress & fifthNumberMask) >> 28;
        long sixthNumber = (encodedAddress & sixthNumberMask) >> 24;
        long seventhNumber = (encodedAddress & seventhNumberMask) >> 20;
        long eigthNumber = (encodedAddress & eighthNumberMask) >> 16;
        long ninthNumber = (encodedAddress & ninthNumberMask) >> 12;
        long tenthNumber = (encodedAddress & tenthNumberMask) >> 8;
        long eleventhNumber = (encodedAddress & eleventhNumberMask) >> 4;
        long twelfthNumber = (encodedAddress & twelfthNumberMask);

        phoneNumber = String.valueOf(secondNumber) +
                String.valueOf(firstNumber) +
                String.valueOf(fourthNumber) +
                String.valueOf(thirdNumber) +
                String.valueOf(sixthNumber) +
                String.valueOf(fifthNumber) +
                String.valueOf(eigthNumber) +
                String.valueOf(seventhNumber) +
                String.valueOf(tenthNumber) +
                String.valueOf(ninthNumber) +
                String.valueOf(twelfthNumber) +
                String.valueOf(eleventhNumber);

        return phoneNumber;
    }


    public static String resolveMAPOperation(int operation) {
        switch (operation) {
            case 2:
                return "UpdateLocation";
            case 3:
                return "CancelLocation";
            case 4:
                return "ProvideRoamingNumber";
            case 5:
                return "NoteSubscriberDataModified";
            case 6:
                return "ResumeCallHandling";
            case 7:
                return "InsertSubscriberData";
            case 8:
                return "DeleteSubscriberData";
            case 10:
                return "RegisterSS";
            case 11:
                return "EraseSS";
            case 12:
                return "ActivateSS";
            case 13:
                return "DeactivateSS";
            case 14:
                return "InterrogateSS";
            case 15:
                return "AuthenticationFailureReport";
            case 17:
                return "RegisterPassword";
            case 18:
                return "GetPassword";
            case 20:
                return "ReleaseResources";
            case 21:
                return "ReportSM-DeliveryStatus";
            case 22:
                return "SendRoutingInfo";
            case 23:
                return "UpdateGprsLocation";
            case 24:
                return "SendRoutingInfoForGprs";
            case 25:
                return "FailureReport";
            case 26:
                return "NoteMsPresentForGprs";
            case 29:
                return "SendEndSignal";
            case 33:
                return "ProcessAccessSignalling";
            case 34:
                return "ForwardAccessSignalling";
            case 37:
                return "Reset";
            case 38:
                return "ForwardCheckSS-Indication";
            case 43:
                return "CheckIMEI";
            case 44:
                return "MT-ForwardSM";
            case 45:
                return "SendRoutingInfoForSM";
            case 46:
                return "MO-ForwardSM  ";
            case 47:
                return "ReportSM-DeliveryStatus";
            case 50:
                return "ActivateTraceMode";
            case 51:
                return "DeactivateTraceMode";
            case 55:
                return "SendIdentification";
            case 56:
                return "SendAuthenticationInfo ";
            case 57:
                return "RestoreData";
            case 58:
                return "SendIMSI";
            case 59:
                return "ProcessUnstructuredSS-Request";
            case 60:
                return "UnstructuredSS-Request";
            case 61:
                return "UnstructuredSS-Notify";
            case 62:
                return "AnyTimeSubscriptionInterrogation";
            case 63:
                return "InformServiceCentre";
            case 64:
                return "AlertServiceCentre";
            case 65:
                return "AnyTimeModification";
            case 66:
                return "ReadyForSM";
            case 67:
                return "PurgeMS";
            case 68:
                return "PrepareHandover";
            case 69:
                return "PrepareSubsequentHandover";
            case 70:
                return "provideSubscriberInfo";
            case 71:
                return "AnyTimeInterrogation";
            case 73:
                return "SetReportingState";
            case 74:
                return "StatusReport";
            case 75:
                return "RemoteUserFree";
            case 83:
                return "ProvideSubscriberLocation";
            case 89:
                return "NoteMM-Event";
            default:
                return "Unmapped Operation";
        }
    }
}
