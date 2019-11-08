package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.contracts.types.IssuedTokenType
import com.r3.corda.lib.tokens.contracts.types.TokenType
import com.r3.corda.lib.tokens.contracts.utilities.issuedBy
import com.r3.corda.lib.tokens.contracts.utilities.of
import com.r3.corda.lib.tokens.workflows.utilities.getPreferredNotary
import com.template.contracts.RegisterContract
import com.template.functions.FlowFunctions
import com.template.states.RegisterState
import net.corda.core.contracts.Amount
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import java.lang.IllegalArgumentException


@StartableByRPC
class RegisterYenFlow(private val name: String) : FlowFunctions() {

    @Suspendable
    override fun call(): SignedTransaction {
        return subFlow(FinalityFlow(verifyAndSign(transaction()), listOf()))
    }

    private fun outState() : RegisterState{

        if(ourIdentity != stringToParty("BankJP"))
            throw IllegalArgumentException("Only the BankJP can use in this flow")

        return RegisterState(
                name = name,
                wallet = userWallet(),
                linearId = UniqueIdentifier(),
                participants = listOf(ourIdentity)
        )
    }
    private fun userWallet() : MutableList<Amount<IssuedTokenType>>
    {
        val yen = 0 of TokenType("yen",2) issuedBy stringToParty("BankJP")
        val YENcurrency = 0 of TokenType("YENcurrency", 2) issuedBy stringToParty("BankJP")
        return mutableListOf(yen,YENcurrency)
    }

    private fun transaction() = TransactionBuilder(notary = getPreferredNotary(serviceHub)).apply {
        val cmd = Command(RegisterContract.Commands.Register(), ourIdentity.owningKey)
        addOutputState(outState(), RegisterContract.ID)
        addCommand(cmd)
    }

}