package net.corda.node.serialization.amqp

import net.corda.core.cordapp.Cordapp
import net.corda.core.internal.toSynchronised
import net.corda.core.serialization.ClassWhitelist
import net.corda.core.serialization.SerializationContext
import net.corda.core.serialization.SerializationCustomSerializer
import net.corda.serialization.internal.CordaSerializationMagic
import net.corda.serialization.internal.amqp.AbstractAMQPSerializationScheme
import net.corda.serialization.internal.amqp.AccessOrderLinkedHashMap
import net.corda.serialization.internal.amqp.SerializerFactory
import net.corda.serialization.internal.amqp.SerializerFactoryBuilder
import net.corda.serialization.internal.amqp.custom.RxNotificationSerializer

/**
 * When set as the serialization scheme, defines the RPC Server serialization scheme as using the Corda
 * AMQP implementation.
 */
class AMQPServerSerializationScheme(
        cordappCustomSerializers: Set<SerializationCustomSerializer<*, *>>,
        serializerFactoriesForContexts: MutableMap<Pair<ClassWhitelist, ClassLoader>, SerializerFactory>
) : AbstractAMQPSerializationScheme(cordappCustomSerializers, serializerFactoriesForContexts) {
    constructor(cordapps: List<Cordapp>) : this(cordapps.customSerializers, AccessOrderLinkedHashMap<Pair<ClassWhitelist, ClassLoader>, SerializerFactory>(128).toSynchronised())
    constructor(cordapps: List<Cordapp>, serializerFactoriesForContexts: MutableMap<Pair<ClassWhitelist, ClassLoader>, SerializerFactory>) : this(cordapps.customSerializers, serializerFactoriesForContexts)

    constructor() : this(emptySet(), AccessOrderLinkedHashMap<Pair<ClassWhitelist, ClassLoader>, SerializerFactory>(128).toSynchronised() )

    override fun rpcClientSerializerFactory(context: SerializationContext): SerializerFactory {
        throw UnsupportedOperationException()
    }

    override fun rpcServerSerializerFactory(context: SerializationContext): SerializerFactory {
        return SerializerFactoryBuilder.build(context.whitelist, context.deserializationClassLoader, context.lenientCarpenterEnabled).apply {
            register(RpcServerObservableSerializer())
            register(RpcServerCordaFutureSerializer(this))
            register(RxNotificationSerializer(this))
        }
    }

    override fun canDeserializeVersion(magic: CordaSerializationMagic, target: SerializationContext.UseCase): Boolean {
        return canDeserializeVersion(magic) &&
                (   target == SerializationContext.UseCase.P2P
                 || target == SerializationContext.UseCase.Storage
                 || target == SerializationContext.UseCase.RPCServer)
    }
}
