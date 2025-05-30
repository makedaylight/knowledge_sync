package com.daylightcomputer.hlc.model

import com.daylightcomputer.hlc.exceptions.TimestampFormatException

data class Timestamp(
    val logicalTime: LogicalTimestamp,
    val distributedNode: DistributedNode,
    val counter: Counter,
) : Comparable<Timestamp>,
    Packable<Timestamp> {
    override fun encode(): String =
        "${logicalTime.encode()}-${counter.encode()}-${distributedNode.encode()}"

    override fun compareTo(other: Timestamp): Int {
        val timeCompare = logicalTime.compareTo(other.logicalTime)
        if (timeCompare != 0) return timeCompare

        val counterCompare = counter.compareTo(other.counter)
        if (counterCompare != 0) return counterCompare

        return distributedNode.compareTo(other.distributedNode)
    }

    companion object : Packable.HelpHelp<Timestamp> {
        override val encodedLength: Int
            get() =
                Counter.encodedLength +
                    LogicalTimestamp.encodedLength +
                    DistributedNode.encodedLength + 2

        override fun fromEncodedImpl(data: String): Timestamp {
            try {
                val timeLength = LogicalTimestamp.encodedLength
                val counterLength = Counter.encodedLength

                val timeString = data.substring(0, timeLength)
                val counterString =
                    data.substring(
                        timeLength + 1,
                        timeLength + 1 + counterLength,
                    )
                val nodeString =
                    data.substring(timeLength + 1 + counterLength + 1)

                val logicalTime = LogicalTimestamp.fromEncoded(timeString)
                val counter = Counter.fromEncoded(counterString)
                val distributedNode = DistributedNode.fromEncoded(nodeString)

                return Timestamp(logicalTime, distributedNode, counter)
            } catch (e: Exception) {
                when (e) {
                    is TimestampFormatException -> throw e
                    is StringIndexOutOfBoundsException ->
                        throw TimestampFormatException(
                            "Invalid timestamp format: $data",
                        )

                    else -> throw TimestampFormatException(
                        "Failed to parse timestamp: " +
                            "$data. ${e.message}",
                    )
                }
            }
        }
    }
}
