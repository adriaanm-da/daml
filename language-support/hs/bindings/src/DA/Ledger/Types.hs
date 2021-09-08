-- Copyright (c) 2021 Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
-- SPDX-License-Identifier: Apache-2.0

{-# LANGUAGE DuplicateRecordFields #-}

-- These types offer the following benefits over the LowLevel types
--
-- (1) These types are human curated and intended for human consumption.
--     (The lowlevel types are generated by compile-proto,
--      and have verbose record-field and constructor names.)
-- (2) These types are stronger:
--      distinguishing various identifier classes, instead of everywhere being `Text`.
-- (3) These types capture required-field invariants.
-- (4) These types form a barrier against changes to names & representation in the .proto files.

module DA.Ledger.Types( -- High Level types for communication over Ledger API

    AbsOffset(..),
    Checkpoint(..),
    Choice(..),
    Command(..),
    Commands(..),
    Completion(..),
    EntityName(..),
    Enum(..),
    Event(..),
    Identifier(..),
    LedgerConfiguration(..),
    LedgerOffset(..),
    ModuleName(..),
    Party(..),
    Record(..),
    RecordField(..),
    Timestamp(..),
    Transaction(..),
    TransactionTree(..),
    TreeEvent(..),
    Value(..),
    Variant(..),
    Verbosity(..),

    MicroSecondsSinceEpoch(..),
    DaysSinceEpoch(..),

    ApplicationId(..),
    CommandId(..),
    ConstructorId(..),
    ContractId(..),
    EventId(..),
    LedgerId(..),
    PackageId(..),
    TemplateId(..),
    TransactionId(..),
    WorkflowId(..),
    SubmissionId(..),
    LL.Duration(..),
    LL.Status(..),
    DeduplicationPeriod(..)
    ) where

import qualified Data.Aeson as A
import Data.Fixed
import Data.Int (Int64)
import Data.Map (Map)
import Data.Text.Lazy (Text)
import Prelude hiding(Enum)
import qualified Data.Text.Lazy as Text(unpack)
import qualified Google.Protobuf.Duration as LL
import qualified Google.Rpc.Status as LL

-- commands.proto

data Commands = Commands
    { lid          :: LedgerId
    , wid          :: Maybe WorkflowId
    , aid          :: ApplicationId
    , cid          :: CommandId
    , actAs        :: [Party]
    , readAs       :: [Party]
    , dedupPeriod  :: Maybe DeduplicationPeriod
    , coms         :: [Command]
    , minLeTimeAbs :: Maybe Timestamp
    , minLeTimeRel :: Maybe LL.Duration
    , sid          :: Maybe SubmissionId
    }

data Command
    = CreateCommand
      { tid  :: TemplateId
      , args :: Record
      }
    | ExerciseCommand
      { tid    :: TemplateId
      , cid    :: ContractId
      , choice :: Choice
      , arg    :: Value
      }
    | CreateAndExerciseCommand
      { tid        :: TemplateId
      , createArgs :: Record
      , choice     :: Choice
      , choiceArg  :: Value
      }
    deriving (Eq,Ord,Show)

data DeduplicationPeriod
    = DeduplicationDuration LL.Duration
  deriving (Eq, Ord, Show)

-- ledger_offset.proto

data LedgerOffset = LedgerBegin | LedgerEnd | LedgerAbsOffset AbsOffset
    deriving (Eq,Ord,Show)

-- completion.proto

data Completion = Completion
    { cid    :: CommandId
    , status :: Maybe LL.Status
    }
    deriving (Eq,Ord,Show)

data Checkpoint = Checkpoint -- TODO: add time info
    { offset :: AbsOffset
    }
    deriving (Eq,Ord,Show)

-- transaction.proto

data Transaction = Transaction
    { trid   :: TransactionId
    , cid    :: Maybe CommandId
    , wid    :: Maybe WorkflowId
    , leTime :: Timestamp
    , events :: [Event]
    , offset :: AbsOffset
    }
    deriving (Eq,Ord,Show)

data TransactionTree = TransactionTree
    { trid   :: TransactionId
    , cid    :: Maybe CommandId
    , wid    :: Maybe WorkflowId
    , leTime :: Timestamp
    , offset :: AbsOffset
    , events :: Map EventId TreeEvent
    , roots  :: [EventId]
    }
    deriving (Eq,Ord,Show)

data TreeEvent
    = CreatedTreeEvent -- TODO: dedup TreeEvent / Event ! ?
      { eid         :: EventId
      , cid         :: ContractId
      , tid         :: TemplateId
      , createArgs  :: Record
      , witness     :: [Party]
      , key         :: Maybe Value
      , signatories :: [Party]
      , observers   :: [Party]
      }
    | ExercisedTreeEvent
      { eid       :: EventId
      , cid       :: ContractId
      , tid       :: TemplateId
      , choice    :: Choice
      , choiceArg :: Value
      , acting    :: [Party]
      , consuming :: Bool
      , witness   :: [Party]
      , childEids :: [EventId]
      , result    :: Value
      }
    deriving (Eq,Ord,Show)

-- event.proto

data Event
    = CreatedEvent
      { eid         :: EventId
      , cid         :: ContractId
      , tid         :: TemplateId
      , createArgs  :: Record
      , witness     :: [Party]
      , key         :: Maybe Value
      , signatories :: [Party]
      , observers   :: [Party]
      }
    | ArchivedEvent
      { eid     :: EventId
      , cid     :: ContractId
      , tid     :: TemplateId
      , witness :: [Party]
      }
    deriving (Eq,Ord,Show)

-- value.proto

data E10
instance HasResolution E10 where
    resolution _ = 10000000000 -- 10^-10 resolution

data Value
    = VRecord Record
    | VVariant Variant
    | VContract ContractId
    | VList [Value]
    | VInt Int
    | VDecimal (Fixed E10)
    | VText Text
    | VTime MicroSecondsSinceEpoch
    | VParty Party
    | VBool Bool
    | VUnit
    | VDate DaysSinceEpoch
    | VOpt (Maybe Value)
    | VMap (Map Text Value)
    | VGenMap [(Value, Value)] -- GenMap is sensitive to order.
    | VEnum Enum
    deriving (Eq,Ord,Show)

data Enum = Enum
    { eid   :: Maybe Identifier
    , cons  :: ConstructorId
    }
    deriving (Eq,Ord,Show)

data Record = Record
    { rid    :: Maybe Identifier
    , fields :: [RecordField]
    }
    deriving (Eq,Ord,Show)

data RecordField = RecordField
    { label :: Text
    , fieldValue :: Value
    }
    deriving (Eq,Ord,Show)

data Variant = Variant
    { vid   :: Maybe Identifier
    , cons  :: ConstructorId
    , value :: Value
    }
    deriving (Eq,Ord,Show)

data Identifier = Identifier
    { pid :: PackageId
    , mod :: ModuleName
    , ent :: EntityName
    }
    deriving (Eq,Ord,Show)

data Timestamp = Timestamp
    { seconds :: Int
    , nanos   :: Int
    }
    deriving (Eq,Ord,Show)

data LedgerConfiguration = LedgerConfiguration
    { maxDeduplicationTime :: LL.Duration
    }
    deriving (Eq,Ord,Show)

newtype MicroSecondsSinceEpoch = MicroSecondsSinceEpoch { unMicroSecondsSinceEpoch :: Int64}
    deriving (Eq,Ord,Show)

newtype DaysSinceEpoch = DaysSinceEpoch { unDaysSinceEpoch :: Int}
    deriving (Eq,Ord,Show)

newtype TemplateId = TemplateId Identifier deriving (Eq,Ord,Show)

newtype ApplicationId = ApplicationId { unApplicationId :: Text } deriving (Eq,Ord,Show)
newtype CommandId = CommandId { unCommandId :: Text } deriving (Eq,Ord,Show)
newtype ConstructorId = ConstructorId { unConstructorId :: Text } deriving (Eq,Ord,Show)
newtype ContractId = ContractId { unContractId :: Text } deriving (Eq,Ord,Show)
newtype EventId = EventId { unEventId :: Text } deriving (Eq,Ord,Show)
newtype LedgerId = LedgerId { unLedgerId :: Text } deriving (Eq,Ord,Show)
newtype PackageId = PackageId { unPackageId :: Text } deriving (Eq,Ord,Show)
newtype TransactionId = TransactionId { unTransactionId :: Text } deriving (Eq,Ord,Show)
newtype WorkflowId = WorkflowId { unWorkflowId :: Text } deriving (Eq,Ord,Show)
newtype SubmissionId = SubmissionId { unSubmissionId :: Text } deriving (Eq,Ord,Show)

newtype ModuleName = ModuleName { unModuleName :: Text } deriving (Eq,Ord,Show)
newtype EntityName = EntityName { unEntityName :: Text } deriving (Eq,Ord,Show)
newtype AbsOffset = AbsOffset { unAbsOffset :: Text } deriving (Eq,Ord,Show)

newtype Choice = Choice { unChoice :: Text } deriving (Eq,Ord,Show)

newtype Party = Party { unParty :: Text } deriving (Eq,Ord)
instance Show Party where show p = "'" <> Text.unpack (unParty p) <> "'"
instance A.FromJSON Party where
  parseJSON v = Party <$> A.parseJSON v

newtype Verbosity = Verbosity { unVerbosity :: Bool } deriving (Eq,Ord,Show)
